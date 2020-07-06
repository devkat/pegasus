package devkat.pegasus.view

import devkat.pegasus.model.EditorModel.Flow
import diode.ModelRO

object FlowView {

  def render(flow: ModelRO[Flow]) =
    flow.value.map(ElementView.render)

}
