name := "Pegasus Authoring System"

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "devkat"
ThisBuild / version := "0.1.0"

lazy val commonSettings = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
  scalacOptions ++= Seq(
    "-language:higherKinds"
  )
)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.1.1",
      "com.beachape" %%% "enumeratum-circe" % "1.6.1"
    ) ++ Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser"
    ).map(_ % "0.13.0")
  )

/*
lazy val shared = project.in(file("shared")).
  aggregate(sharedCross.js, sharedCross.jvm).
  settings(
    publish := {},
    publishLocal := {},
  )
*/

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
  .dependsOn(shared.js)
  .enablePlugins(ScalaJSPlugin)

lazy val server = (project in file("server"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.xmlgraphics" % "fop" % "2.5" excludeAll(
        ExclusionRule(organization = "javax.media"),
        ExclusionRule(organization = "com.sun.media"),
      )
    ) ++ Seq(
      "org.http4s" %% "http4s-dsl",
      "org.http4s" %% "http4s-blaze-server",
      "org.http4s" %% "http4s-circe"
    ).map(_ % "1.0.0-M3")
  )
  .dependsOn(shared.jvm)