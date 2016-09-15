name := "Pegasus Authoring System"

lazy val commonSettings = Seq(
  organization := "net.devkat",
  version := "0.1.0",
  scalaVersion := "2.11.8"
)

val diodeVersion = "1.0.0"

lazy val client = (project in file("client")).
  settings(commonSettings).
  settings(
    workbenchSettings,
    bootSnippet := "Pegasus().main();",
    testFrameworks += new TestFramework("utest.runner.Framework"),
    emitSourceMaps := true,
    /* create javascript launcher. Searches for an object extends JSApp */
    persistLauncher := true,

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "com.github.japgolly.scalajs-react" %%% "core" % "0.11.1",
      "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.1",
      "me.chrons" %%% "diode" % diodeVersion,
      "me.chrons" %%% "diode-devtools" % diodeVersion,
      "me.chrons" %%% "diode-react" % diodeVersion,
      "me.chrons" %%% "boopickle" % "1.2.1"
    ),

    jsDependencies ++= Seq(
      "org.webjars.bower" % "react" % "15.1.0" / "react-with-addons.js" commonJSName "React" minified "react-with-addons.min.js",
      "org.webjars.bower" % "react" % "15.1.0" / "react-dom.js" commonJSName "ReactDOM" minified "react-dom.min.js" dependsOn "react-with-addons.js"
    )
  ).enablePlugins(ScalaJSPlugin, SbtWeb)