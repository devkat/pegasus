package devkat.pegasus

import com.raquo.laminar.api.L._
import org.scalajs.dom.document

object App {

  def main(args: Array[String]): Unit = {

    // Wait until the DOM is loaded, otherwise app-container element might not exist
    documentEvents.onDomContentLoaded.foreach { _ =>

      val container = document.getElementById("app-container") // This div, its id and contents are defined in index-fastopt.html/index-fullopt.html files
      container.textContent = ""

      render(container, Pegasus.render())

      //      render(container, TodoApp())
      //      render(container, SvgContainer())
      //      render(container, DuckMaster.app())
    }(unsafeWindowOwner)

  }

}
