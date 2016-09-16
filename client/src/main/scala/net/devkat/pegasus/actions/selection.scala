package net.devkat.pegasus.actions

import diode.{Action, ActionHandler, ModelRW}
import net.devkat.pegasus.model.{Position, Selection}

case class SetSelection(selection: Selection) extends Action

object ClearSelection extends Action


class SelectionHandler[M](modelRW: ModelRW[M, Option[Selection]]) extends ActionHandler(modelRW) {
  override def handle = {

    case SetSelection(selection) => updated(Some(selection))

    case ClearSelection => updated(None)

  }
}