addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.1.1")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.10")

//addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.18.0")

resolvers += Resolver.bintrayRepo("oyvindberg", "converter")

addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta25")
