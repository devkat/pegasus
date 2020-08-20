package devkat.pegasus

import cats.implicits._
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

      private def startEnd(selection: Selection) = {
        val start = Math.min(selection.anchor, selection.focus)
        val end = Math.max(selection.anchor, selection.focus)
        (start, end)
      }

      override def handle = {

        case ReplaceFlow(flow) =>
          updated(value.copy(flow = flow, selection = Some(Selection(flow.length, flow.length))))

        case Insert(c) =>
          value
            .selection
            .fold(noChange) { selection =>
              updated {
                val (start, end) = startEnd(selection)
                value.copy(
                  flow = value.flow.take(start) ::: List(Character(c, CharacterStyle.empty)) ::: value.flow.drop(end),
                  selection = Some(Selection(start + 1, start + 1))
                )
              }
            }

        case Backspace =>
          value
            .selection
            .fold(noChange) { selection =>
              val (start, end) = startEnd(selection)
              if (start === end) {
                if (start > 0) {
                  updated(
                    value.copy(
                      flow = value.flow.take(start - 1) ::: value.flow.drop(end),
                      selection = Some(Selection(start - 1, start - 1))
                    )
                  )
                } else {
                  noChange
                }
              } else {
                updated(
                  value.copy(
                    flow = value.flow.take(start) ::: value.flow.drop(end),
                    selection = Some(Selection(start, start))
                  )
                )
              }
            }

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