name := "Pegasus Authoring System"

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "devkat"
ThisBuild / version := "0.1.0"

lazy val sharedCross = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))

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
      "com.raquo" %%% "laminar" % "0.9.0"
    )
  )
  .dependsOn(sharedCross.js)
  .enablePlugins(ScalaJSPlugin)

