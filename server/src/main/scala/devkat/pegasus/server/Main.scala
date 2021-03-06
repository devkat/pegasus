package devkat.pegasus.server

import cats.effect._
import cats.implicits._
import devkat.pegasus.fonts.FontService
import devkat.pegasus.fonts.FontFamily
import devkat.pegasus.hyphenation.HyphenationService
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.Server
import org.http4s.server.staticcontent._
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    app.use(_ => IO.never).as(ExitCode.Success)

  private val fontService =
    HttpRoutes.of[IO] {
      case GET -> Root / "fonts" ~ "json" =>
        FontService.getFonts[IO]
          .flatMap(Ok(_))
          .handleErrorWith(t => InternalServerError(t.getMessage))
    }

  private val hyphenationService =
    HttpRoutes.of[IO] {
      case GET -> Root / "hyphenation" / lang ~ "json" =>
        HyphenationService[IO](lang)
          .flatMap(Ok(_))
          .handleErrorWith(t => InternalServerError(t.getMessage))
    }

  val app: Resource[IO, Server] =
    for {
      blocker <- Blocker[IO]
      server <- BlazeServerBuilder[IO](global)
        .bindHttp(8080)
        .withHttpApp(
          (
            fontService <+>
              hyphenationService <+>
              fileService[IO](FileService.Config("client/target/scala-2.13", blocker)) /* <+>
              resourceServiceBuilder[IO]("/", blocker).toRoutes */
            ).orNotFound
        )
        .resource
    } yield server

}
