package devkat.pegasus

import devkat.pegasus.Actions.{LoadFonts, LoadHyphenationSpec}
import devkat.pegasus.view.Editor
import diode.data.Pot
import japgolly.scalajs.react.extra.router.{BaseUrl, Router, RouterConfig, RouterConfigDsl, SetRouteVia}
import org.scalajs.dom

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object App {

  private val baseUrl: BaseUrl =
    BaseUrl(dom.window.location.href.takeWhile(_ != '#'))

  private val routerConfig: RouterConfig[Unit] =
    RouterConfigDsl[Unit].buildConfig { dsl =>
      import dsl._

      val connection = AppCircuit.connect(_.editor)

      lazy val editorRoute: Rule =
        staticRoute("#/", ()) ~> renderR(router => connection(p => Editor(p)))

      /*
      val editor = AppCircuit.wrap(m => m)(m => Editor(m))
      lazy val editorRoute: Rule =
        staticRoute("#/", ()) ~> renderR(_ => editor)
       */

      editorRoute.notFound(redirectToPage(())(SetRouteVia.HistoryReplace))
    }

  def main(args: Array[String]): Unit = {

    //val rootModel = AppCircuit.zoom(identity)
    //AppCircuit.subscribe(rootModel)(_ => render())

    AppCircuit.dispatch(LoadFonts(Pot.empty))
    AppCircuit.dispatch(LoadHyphenationSpec(Pot.empty))

    val router = Router(baseUrl, routerConfig.logToConsole)
    router().renderIntoDOM(dom.document.getElementById("app-container"))

  }


}
