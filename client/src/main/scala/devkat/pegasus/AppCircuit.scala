package devkat.pegasus

import devkat.pegasus.Actions.{Insert, LoadFonts, ReplaceFlow}
import devkat.pegasus.examples.Lipsum
import devkat.pegasus.model.EditorModel
import devkat.pegasus.model.EditorModel.Element.Glyph
import devkat.pegasus.fonts.FontFamily
import diode._
import diode.data.PotState._
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AppCircuit extends Circuit[EditorModel] {

  override def initialModel: EditorModel = EditorModel.fromFlow(Lipsum.flowFromString(Lipsum.lipsum))

  val flowHandler = new ActionHandler(zoomTo(_.flow)) {
    override def handle = {

        case ReplaceFlow(flow) => updated(flow)

        case Insert(c) => updated(Glyph(c, Set.empty) +: value)

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
      .flatMap(r => Future.fromTry(r.as[List[FontFamily]].toTry))
  }

}