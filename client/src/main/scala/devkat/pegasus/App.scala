package devkat.pegasus

import devkat.pegasus.Actions.ReplaceFlow
import devkat.pegasus.examples.Lipsum
import devkat.pegasus.model.EditorModel
import devkat.pegasus.view.RootModelView
import diode.ModelRO
import scalatags.JsDom.all._
import org.scalajs.dom

object App {

  def main(args: Array[String]): Unit = {

    val rootModel = AppCircuit.zoom(identity)

    // subscribe to changes in the application model and call render when anything changes
    AppCircuit.subscribe(rootModel)(_ => render(rootModel))

    // start the application by dispatching a ReplaceTree action
    AppCircuit.dispatch(ReplaceFlow(AppCircuit.initialModel.flow))

  }

  def render(rootModel: ModelRO[EditorModel]) = {

    val e = div(
      cls := "app-container",
      h1("Editor"),
      RootModelView.render(rootModel, AppCircuit)
    ).render

    // clear and update contents
    dom.document.body.innerHTML = ""
    dom.document.body.appendChild(e)
  }

}