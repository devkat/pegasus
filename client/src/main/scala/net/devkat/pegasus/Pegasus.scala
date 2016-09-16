package net.devkat.pegasus

import boopickle.Default._
import diode.dev.{Hooks, PersistStateIDB}
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router._
import net.devkat.pegasus.model.RootModel
import net.devkat.pegasus.pages.Page
import net.devkat.pegasus.view.Homepage

import scala.scalajs.js.typedarray.TypedArrayBufferOps._
import scala.scalajs.js.typedarray._

@JSExport("Pegasus")
object Pegasus extends JSApp {

  val baseUrl = BaseUrl(dom.window.location.href.takeWhile(_ != '#'))

  val routerConfig: RouterConfig[Page] = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    val flowConnection = AppCircuit.connect(m => m)

    (
      staticRoute(root, Page.Homepage) ~> renderR(router => flowConnection(p => Homepage(router, p)))
    ).notFound(redirectToPage(Page.Homepage)(Redirect.Replace))
  }

  val router = Router(BaseUrl.until_#, routerConfig)()

  /**
    * Function to pickle application model into a TypedArray
    *
    * @param model
    * @return
  def pickle(model: RootModel) = {
    val data = Pickle.intoBytes(model)
    data.typedArray().subarray(data.position, data.limit)
  }
    */

  /**
    * Function to unpickle application model from a TypedArray
    *
    * @param data
    * @return
  def unpickle(data: Int8Array) = {
    Unpickle[RootModel].fromBytes(TypedArrayBuffer.wrap(data))
  }
    */

  @JSExport
  override def main(): Unit = {
    // add a development tool to persist application state
    //AppCircuit.addProcessor(new PersistStateIDB(pickle, unpickle))

    // hook it into Ctrl+Shift+S and Ctrl+Shift+L
    //Hooks.hookPersistState("test", AppCircuit)

    ReactDOM.render(router, dom.document.getElementById("editor"))
  }
}