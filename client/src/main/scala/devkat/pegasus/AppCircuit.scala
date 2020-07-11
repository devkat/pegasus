package devkat.pegasus

import devkat.pegasus.Actions.{Insert, LoadFonts, ReplaceFlow}
import devkat.pegasus.examples.Lipsum
import devkat.pegasus.fonts.{FontFamily, Fonts}
import devkat.pegasus.model.EditorModel
import devkat.pegasus.model.Style.CharacterStyle
import devkat.pegasus.model.sequential.{Character, Flow}
import diode._
import diode.data.Pot
import diode.data.PotState._
import org.scalajs.dom
import shapeless.HMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AppCircuit extends Circuit[EditorModel] {

  override def initialModel: EditorModel = {
    EditorModel(
      flow = Flow.fromNestedFlow(Lipsum.flowFromString(Lipsum.lipsum)),
      selection = None,
      fonts = Pot.empty,
      status = None
    )
  }

  val flowHandler = new ActionHandler(zoomTo(_.flow)) {
    override def handle = {

        case ReplaceFlow(flow) => updated(flow)

        case Insert(c) => updated(value :+ Character(c, HMap.empty))

      }
  }

  val fontHandler = new ActionHandler(zoomTo(_.fonts)) {
    override def handle = {
      case action: LoadFonts =>
        val updateEffect = action.effect(loadFonts())(identity)
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
            val ex = action.result.failed.get
            updated(value.fail(ex))
        }
    }
  }

  override val actionHandler = composeHandlers(flowHandler, fontHandler)

  private def loadFonts() = {
    import io.circe.parser._

    dom.ext.Ajax
      .get(url = "/fonts.json")
      .flatMap(r => Future.fromTry(parse(r.responseText).toTry))
      .flatMap(r => Future.fromTry(r.as[Fonts].toTry))
  }

}