package devkat.pegasus.view

import cats.Eq
import cats.implicits._
import devkat.pegasus.model.Style.CharacterStyle
import devkat.pegasus.model.Style.CharacterStyle._
import devkat.pegasus.model.StyleAttr._
import shapeless.HMap

object OrphanShapelessInstances {

  // implicit def hMapInstance[R[_, _]]: Eq[HMap[R]] = ???

  implicit def characterStyleInstance: Eq[HMap[CharacterStyle]] =
    new Eq[HMap[CharacterStyle]] {

      override def eqv(x: HMap[CharacterStyle], y: HMap[CharacterStyle]): Boolean = {
        def eq[K, V: Eq](key: K)(implicit ev: CharacterStyle[K, V]): Boolean =
          x.get(key) === y.get(key)

        eq(FontFamily)(Eq[String], fontFamily) &&
          eq(FontStyle)(Eq[String], fontStyle) &&
          eq(FontWeight)(Eq[Int], fontWeight) &&
          eq(FontSize)(Eq[Int], fontSize) &&
          eq(Color)(Eq[String], color)
      }
    }

}
