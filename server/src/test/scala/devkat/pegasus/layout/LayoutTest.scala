package devkat.pegasus.layout

import cats.Eval
import cats.effect.IO
import devkat.pegasus.examples.Examples._
import devkat.pegasus.fonts.FontService
import devkat.pegasus.hyphenation.HyphenationSpec
import devkat.pegasus.model.sequential.Flow
import org.specs2.mutable.Specification

object LayoutTest extends Specification {

  private lazy val fonts = FontService.getFonts[IO].unsafeRunSync

  "The layout" should {

    "process large files" in {

      val flow: Flow = Flow.fromNestedFlow(flowFromString((1 to 10).map(_ => dummyText).mkString))

      val env = LayoutEnv(
        LayoutSettings(
          showHiddenCharacters = true,
          hyphenate = true
        ),
        fonts,
        HyphenationSpec(Nil, Nil)
      )

      val (log, _, lines) = Layout[Eval](flow, 500).run(env, ()).value

      lines must not be empty
      log must be empty

    }

  }

}
