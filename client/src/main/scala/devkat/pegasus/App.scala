package devkat.pegasus

import devkat.pegasus.Actions.{LoadFonts, LoadHyphenationSpec}
import devkat.pegasus.model.editor.EditorModel
import devkat.pegasus.view.RootModelView
import diode.ModelRO
import diode.data.Pot
import org.scalajs.dom
import scalatags.JsDom.all._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object App {

  def main(args: Array[String]): Unit = {

    val rootModel = AppCircuit.zoom(identity)

    AppCircuit.subscribe(rootModel)(_ => render(rootModel))

    AppCircuit.dispatch(LoadFonts(Pot.empty))
    AppCircuit.dispatch(LoadHyphenationSpec(Pot.empty))

  }

  def render(rootModel: ModelRO[EditorModel]): Unit = {

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
