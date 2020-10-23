name := "Pegasus Authoring System"

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / organization := "devkat"
ThisBuild / version := "0.1.0"

lazy val commonSettings = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
  scalacOptions ++= Seq(
    "-language:higherKinds",
    "-Ymacro-annotations"
  ),
  wartremoverErrors ++= Warts.allBut(Wart.Any, Wart.Nothing, Wart.ToString, Wart.NonUnitStatements)
)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.1.1",
      "org.typelevel" %% "mouse" % "0.25",
      "com.beachape" %%% "enumeratum-circe" % "1.6.1"
    ) ++ Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser"
    ).map(_ % "0.13.0") ++ Seq(
      "org.specs2" %% "specs2-core" % "4.10.0"
    ).map(_  % "test")
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
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    //testFrameworks += new TestFramework("utest.runner.Framework"),
    //emitSourceMaps := true,
    //scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    stFlavour := Flavour.Japgolly,
    stIgnore := List("typeface-roboto"),
    webpackConfigFile := Some(baseDirectory.value / "custom.webpack.config.js"),

    libraryDependencies ++= Seq(
      "io.suzaku" %%% "diode-react" % "1.1.13",
      "com.github.japgolly.scalajs-react" %%% "extra" % "1.7.5",
      "com.payalabs" %%% "scalajs-react-bridge" % "0.8.3-SNAPSHOT",
      "com.github.julien-truffaut" %%% "monocle-core"  % "2.0.3",
      "com.github.julien-truffaut" %%% "monocle-macro" % "2.0.3"
    ),

    npmDependencies in Compile ++= Seq(
      "react" -> "^16.13.1",
      "@types/react" -> "^16.13.1",
      "react-dom" -> "^16.13.1",
      "@types/react-dom" -> "^16.13.1",
      "@material-ui/core" -> "3.9.4",
      //"@material-ui/core" -> "4.11.0",
      "@material-ui/styles" -> "3.0.0-alpha.10",
      "@material-ui/icons" -> "3.0.2",
      "typeface-roboto" -> "0.0.75",
      //"@fortawesome/fontawesome-free" -> "5.7.2"
    ),

    npmDevDependencies in Compile ++= Seq(
      "webpack-merge" -> "^5.1.4",
      "style-loader" -> "^1.2.1",
      "css-loader" -> "^4.3.0",
      "file-loader" -> "^6.1.0",
      "url-loader" -> "^4.1.0"
    )

  )
  .dependsOn(shared.js)
  .enablePlugins(ScalaJSBundlerPlugin, ScalablyTypedConverterPlugin)

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
  .dependsOn(shared.jvm % "compile->compile;test->test")