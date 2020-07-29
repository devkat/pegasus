package devkat.pegasus

import devkat.pegasus.Actions.{Delete, _}
import devkat.pegasus.examples.Examples
import devkat.pegasus.fonts.Fonts
import devkat.pegasus.hyphenation.HyphenationSpec
import devkat.pegasus.model.CharacterStyle
import devkat.pegasus.model.editor.{EditorModel, RootModel, Selection}
import devkat.pegasus.model.sequential.{Character, Flow}
import diode._
import diode.data.Pot
import diode.data.PotState._
import diode.react.ReactConnector
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import io.circe.parser._

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {

  override def initialModel: RootModel = {
    val flow = Flow.fromNestedFlow(Examples.flowFromString(Examples.dummyText))
    RootModel(
      EditorModel(
        flow = flow,
        selection = Some(Selection(flow.length, flow.length)),
        fonts = Pot.empty,
        hyphenationSpec = Pot.empty,
        status = None
      )
    )
  }

  private val flowHandler: ActionHandler[RootModel, EditorModel] =
    new ActionHandler(zoomTo(_.editor)) {
      override def handle = {

        case ReplaceFlow(flow) =>
          updated(value.copy(flow = flow, selection = Some(Selection(flow.length, flow.length))))

        case Insert(c) =>
          updated(value.copy(flow = value.flow :+ Character(c, CharacterStyle.empty)))

        case Delete(start, end) =>
          updated(value.copy(
            flow = value.flow.take(start) ::: value.flow.drop(end),
            selection = Some(Selection(start, start))
          ))

        case SetCaret(index) =>
          updated(value.copy(selection = Some(Selection(index, index))))

      }
    }

  private val fontHandler: ActionHandler[RootModel, Pot[Fonts]] =
    new ActionHandler(zoomTo(_.editor.fonts)) {
      override def handle = {
        case action: LoadFonts =>
          val updateEffect = action.effect(loadFonts)(identity)
          action.handle {
            case PotEmpty =>
              updated(value.pending(), updateEffect)
            case PotPending =>
              noChange
            case PotReady =>
              updated(action.potResult)
            case PotUnavailable =>
              updated(value.unavailable())
            case PotFailed =>
              action.result.failed.fold(
                throwable => updated(value.fail(throwable)),
                _ => noChange
              )

          }
      }
    }

  private val hyphenationHandler: ActionHandler[RootModel, Pot[HyphenationSpec]] =
    new ActionHandler(zoomTo(_.editor.hyphenationSpec)) {
      override def handle = {
        case action: LoadHyphenationSpec =>
          val updateEffect = action.effect(loadHyphenationSpec)(identity)
          action.handle {
            case PotEmpty =>
              updated(value.pending(), updateEffect)
            case PotPending =>
              noChange
            case PotReady =>
              updated(action.potResult)
            case PotUnavailable =>
              updated(value.unavailable())
            case PotFailed =>
              action.result.failed.fold(
                throwable => updated(value.fail(throwable)),
                _ => noChange
              )
          }
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

}