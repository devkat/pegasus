name := "Pegasus Authoring System"

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "devkat"
ThisBuild / version := "0.1.0"

lazy val sharedCross = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(

    Compile / resourceGenerators += Def.task {
      import _root_.io.circe._
      import _root_.io.circe.generic.auto._
      import _root_.io.circe.parser._
      import _root_.io.circe.syntax._

      val log = streams.value.log
      log.info("Generating font info")
      val file = (Compile / resourceManaged).value / "fonts" / "fonts.json"
      val json = FontManager.getAllFonts(log).asJson.spaces2
      IO.write(file, json)
      Seq(file)
    }.taskValue

  )

lazy val shared = project.in(file("shared")).
  aggregate(sharedCross.js, sharedCross.jvm).
  settings(
    publish := {},
    publishLocal := {},
  )

lazy val client = (project in file("client"))
  .settings(
    scalaJSUseMainModuleInitializer := true,
    //testFrameworks += new TestFramework("utest.runner.Framework"),
    //emitSourceMaps := true,
    libraryDependencies ++= Seq(
      "io.suzaku" %%% "diode" % "1.1.10",
      "com.lihaoyi" %%% "scalatags" % "0.9.1"
    )
  )
  .dependsOn(sharedCross.js, shared)
  .enablePlugins(ScalaJSPlugin)

