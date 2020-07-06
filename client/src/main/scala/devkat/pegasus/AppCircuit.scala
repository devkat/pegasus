package devkat.pegasus

import devkat.pegasus.Actions.{Insert, ReplaceFlow}
import devkat.pegasus.examples.Lipsum
import devkat.pegasus.model.EditorModel
import devkat.pegasus.model.EditorModel.Element.Glyph
import diode.ActionResult.ModelUpdate
import diode.Circuit

object AppCircuit extends Circuit[EditorModel] {

  override def initialModel: EditorModel = EditorModel.fromFlow(Lipsum.flowFromString(Lipsum.lipsum))

  override val actionHandler: HandlerFunction =
    (model, action) =>
      action match {
        case ReplaceFlow(flow) => Some(ModelUpdate(model.copy(flow = flow)))
        case Insert(c) =>
          Some(
            ModelUpdate(model.copy(flow = Glyph(c) +: model.flow))
          )
      }

}