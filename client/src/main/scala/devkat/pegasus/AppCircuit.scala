package devkat.pegasus

import cats.Eval
import cats.implicits._
import devkat.pegasus.Actions._
import devkat.pegasus.examples.Examples
import devkat.pegasus.fonts.Fonts
import devkat.pegasus.hyphenation.HyphenationSpec
import devkat.pegasus.layout.{Layout, LayoutEnv, LayoutSettings, Line}
import devkat.pegasus.model.CharacterStyle
import devkat.pegasus.model.editor.{EditorModel, RootModel, Selection}
import devkat.pegasus.model.sequential.{Character, Flow}
import devkat.pegasus.view.{SelectionHelper, SelectionLayout}
import diode._
import diode.data.PotState._
import diode.data.{Pot, PotAction}
import diode.react.ReactConnector
import io.circe.parser._
import monocle.Lens
import monocle.macros.GenLens
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {

  private lazy val layoutSettings: LayoutSettings =
    LayoutSettings(
      showHiddenCharacters = true,
      hyphenate = true
    )

  override def initialModel: RootModel = {
    val flow = Flow.fromNestedFlow(Examples.flowFromString(Examples.dummyText))
    RootModel(
      EditorModel(
        flow = flow,
        layout = Nil,
        selection = None,
        selectionView = None,
        fonts = Pot.empty,
        hyphenationSpec = Pot.empty,
        status = None
      )
    )
  }

  private val flowHandler: ActionHandler[RootModel, EditorModel] =
    new ActionHandler(zoomTo(_.editor)) {

      override def handle = {

        def startEnd(selection: Selection) = {
          val start = Math.min(selection.anchor, selection.focus)
          val end = Math.max(selection.anchor, selection.focus)
          (start, end)
        }

        def updateFlow(flow: Flow): ActionResult[RootModel] =
          updated(updateModel(value, value.copy(flow = flow)))

        def updateSelection(selection: Option[Selection]) =
          updated(updateModel(value, value.copy(selection = selection)))

        def updateFlowAndSelection(flow: Flow, selection: Option[Selection]): ActionResult[RootModel] =
          updated(updateModel(value, value.copy(flow = flow, selection = selection)))

        {

          case ReplaceFlow(flow) =>
            updated(updateModel(value, value.copy(flow = flow, selection = None)))

          case Insert(string) =>
            value
              .selection
              .fold(noChange) { selection =>
                val (start, end) = startEnd(selection)
                val chars = string.map(Character(_, CharacterStyle.empty)).toList
                updateFlowAndSelection(
                  flow = value.flow.take(start) ::: chars ::: value.flow.drop(end),
                  selection = Some(Selection(start + 1, start + 1))
                )
              }

          case Backspace =>
            value
              .selection
              .fold(noChange) { selection =>
                val (start, end) = startEnd(selection)
                if (start === end) {
                  if (start > 1) {
                    updateFlowAndSelection(
                      flow = value.flow.take(start - 1) ::: value.flow.drop(end),
                      selection = Some(Selection(start - 1, start - 1))
                    )
                  } else {
                    noChange
                  }
                } else {
                  updateFlowAndSelection(
                    flow = value.flow.take(start) ::: value.flow.drop(end),
                    selection = Some(Selection(start, start))
                  )
                }
              }

          case MoveCaret(direction) =>
            value.selection.fold(noChange) { selection =>
              val (start, end) = startEnd(selection)
              direction match {

                case Direction.Left =>
                  if (start > 1)
                    updateSelection(Some(Selection(start - 1, start - 1)))
                  else
                    noChange

                case Direction.Right =>
                  if (end < value.flow.length - 1)
                    updateSelection(Some(Selection(end + 1, end + 1)))
                  else
                    noChange

                case Direction.Up =>
                  SelectionHelper
                    .getIndexAbove(value.layout, start)
                    .fold(noChange)(index => updateSelection(Some(Selection(index, index))))

                case Direction.Down =>
                  SelectionHelper
                    .getIndexBelow(value.layout, start)
                    .fold(noChange)(index => updateSelection(Some(Selection(index, index))))

              }
            }

          case SetCaret(index) =>
            updateSelection(Some(Selection(index, index)))

        }
      }
    }

  sealed trait XhrHandler extends ActionHandler[RootModel, EditorModel] {

    def handleXhr[
      A,
      P <: PotAction[A, P]
    ](action: PotAction[A, P],
      load: Future[A],
      lens: Lens[EditorModel, Pot[A]]): ActionResult[RootModel] = {

      val updateEffect = action.effect(load)(identity)
      action.handle {
        case PotEmpty =>
          updated(updateModel(value, lens.modify(_.pending())(value)), updateEffect)
        case PotPending =>
          noChange
        case PotReady =>
          updated(updateModel(value, lens.set(action.potResult)(value)))
        case PotUnavailable =>
          updated(updateModel(value, value.copy(fonts = value.fonts.unavailable())))
        case PotFailed =>
          action.result.failed.fold(
            throwable => updated(updateModel(value, lens.modify(_.fail(throwable))(value))),
            _ => noChange
          )
      }
    }
  }

  private val fontHandler: ActionHandler[RootModel, EditorModel] =
    new ActionHandler(zoomTo(_.editor)) with XhrHandler {
      override def handle = {
        case action: LoadFonts =>
          handleXhr(action, loadFonts, GenLens[EditorModel](_.fonts))
      }
    }

  private val hyphenationHandler: ActionHandler[RootModel, EditorModel] =
    new ActionHandler(zoomTo(_.editor)) with XhrHandler {
      override def handle = {
        case action: LoadHyphenationSpec =>
          handleXhr(action, loadHyphenationSpec, GenLens[EditorModel](_.hyphenationSpec))
      }
    }

  override val actionHandler =
    composeHandlers(
      flowHandler,
      fontHandler,
      hyphenationHandler
    )

  private def loadFonts: Future[Fonts] =
    Ajax
      .get(url = "/fonts.json")
      .flatMap(r => Future.fromTry(parse(r.responseText).toTry))
      .flatMap(r => Future.fromTry(r.as[Fonts].toTry))

  private def loadHyphenationSpec: Future[HyphenationSpec] =
    Ajax
      .get(url = "/hyphenation/en.json")
      .flatMap(r => Future.fromTry(parse(r.responseText).toTry))
      .flatMap(r => Future.fromTry(r.as[HyphenationSpec].toTry))

  private def updateModel(oldModel: EditorModel, newModel: EditorModel): EditorModel = {

    lazy val layout: List[Line] =
      if (newModel.flow =!= oldModel.flow ||
        (newModel.fonts.isReady && !oldModel.fonts.isReady) ||
        (newModel.hyphenationSpec.isReady && !oldModel.hyphenationSpec.isReady))
        Tuple2
          .apply(
            newModel.fonts.toOption,
            newModel.hyphenationSpec.toOption
          )
          .mapN { case (fonts, hyphenationSpec) =>
            val env = LayoutEnv(layoutSettings, fonts, hyphenationSpec)
            val (_, _, lines) = Layout[Eval](newModel.flow, 600).run(env, ()).value
            lines
          }
          .getOrElse(Nil)
      else
        oldModel.layout

    newModel.copy(
      layout = layout,
      selectionView = newModel.selection.flatMap(SelectionLayout(_, layout))
    )
  }


}