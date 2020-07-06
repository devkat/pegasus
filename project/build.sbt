val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "org.apache.xmlgraphics" % "fop" % "2.5" excludeAll(
    ExclusionRule(organization = "javax.media"),
    ExclusionRule(organization = "com.sun.media"),
  )
) ++ Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
