package net.devkat.pegasus

import diode._
import diode.react.ReactConnector
import net.devkat.pegasus.actions.{ClearSelection, InsertCharacter, SetSelection, UpdateFlow}
import net.devkat.pegasus.model._

/**
  * AppCircuit provides the actual instance of the `AppModel` and all the action
  * handlers we need. Everything else comes from the `Circuit`
  */
object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  def initialModel = RootModel(
    Flow(Seq(Section(paragraphs = Seq(Paragraph(elements = Seq(Character(ch = 'a'))))))),
    None
  )

  override val actionHandler = composeHandlers(
    new FlowHandler(zoomRW(_.flow)((m, v) => m.copy(flow = v))),
    new SelectionHandler(zoomRW(_.selection)((m, v) => m.copy(selection = v)))
  )
}

class FlowHandler[M](modelRW: ModelRW[M, Flow]) extends ActionHandler(modelRW) {

  override def handle = {

    case UpdateFlow(flow) => updated(flow)

    case InsertCharacter(selection, ch) => {
      val focus = selection.focus
      modelRW.
        zoom(_.sections.find(_.id == focus.sectionId)).
        zoom(_.pa)
    }

  }
}

class SelectionHandler[M](modelRW: ModelRW[M, Option[Selection]]) extends ActionHandler(modelRW) {
  override def handle = {

    case SetSelection(selection) => updated(Some(selection))

    case ClearSelection => updated(None)

  }
}