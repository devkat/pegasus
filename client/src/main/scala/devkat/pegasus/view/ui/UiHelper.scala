package devkat.pegasus.view.ui

import devkat.pegasus.model.editor.Selection
import devkat.pegasus.model.sequential.{Flow, InlineElement}

object UiHelper {

  def getCommonCharacterStyle[A](flow: Flow, selection: Selection, f: InlineElement => Option[A]): Option[A] = {
    val styles = flow
      .collect { case e: InlineElement => e }
      .map(f)
      .distinct
    styles match {
      case a :: Nil => a
      case _ => None
    }
  }

}
