ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.3"

lazy val root = (project in file("."))
  .settings(
    name := "central-query",
    idePackagePrefix := Some("ims.central.query")
  )

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") => MergeStrategy.singleOrError
  case PathList("META-INF", "resources", "webjars", "swagger-ui", _*)               => MergeStrategy.singleOrError
  case PathList("META-INF", "services", _*)                                         => MergeStrategy.concat
  case PathList("META-INF", _*)                                                     => MergeStrategy.discard
  case x if x.endsWith("module-info.class")                                         => MergeStrategy.concat
  case x => (assembly / assemblyMergeStrategy).value.apply(x)
}

libraryDependencies ++= Seq(
  //Cats Effect
  "org.typelevel" %% "cats-effect" % "3.6.3",

  //Http server and client
  "org.http4s" %% "http4s-core" % "0.23.30",
  "org.http4s" %% "http4s-ember-server" % "0.23.30",
  "org.http4s" %% "http4s-ember-client" % "0.23.30",
  "org.http4s" %% "http4s-circe" % "0.23.30",
  "org.http4s" %% "http4s-dsl" % "0.23.30",

  //Simple Http client
  "com.softwaremill.sttp.client3" %% "core" % "3.11.0",
  "com.softwaremill.sttp.client3" %% "http4s-backend" % "3.11.0",
  "com.softwaremill.sttp.client3" %% "slf4j-backend" % "3.11.0",
  "com.softwaremill.sttp.client3" %% "circe" % "3.11.0",

  //Tapir endpoint descriptors
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.11.44",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.11.44",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.11.44",

  //Doobie SQL Client
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC10",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC10",
  "org.tpolecat" %% "doobie-mysql" % "1.0.0-RC10",

  //Stream Kafka Integration
  "com.github.fd4s" %% "fs2-kafka" % "3.9.0",

  //Circe Json Parsing
  "io.circe" %% "circe-generic" % "0.14.14",
  "io.circe" %% "circe-core" % "0.14.14",
  "io.circe" %% "circe-parser" % "0.14.14",

  //Configuration file mapping
  "com.github.pureconfig" %% "pureconfig-core" % "0.17.9",
  "com.github.pureconfig" %% "pureconfig-generic-scala3" % "0.17.9",
  "com.github.pureconfig" %% "pureconfig-cats" % "0.17.9",
  "com.github.pureconfig" %% "pureconfig-circe" % "0.17.9",
  "com.github.pureconfig" %% "pureconfig-http4s" % "0.17.9",
  "com.github.pureconfig" %% "pureconfig-ip4s" % "0.17.9",
  "com.github.pureconfig" %% "pureconfig-sttp" % "0.17.9",

  //Logging
  "ch.qos.logback" % "logback-classic" % "1.5.18" % Runtime,
  "org.typelevel" %% "log4cats-slf4j" % "2.7.1",

  //Test dependencies
  "org.scalamock" %% "scalamock" % "7.5.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.typelevel" %% "cats-laws" % "2.13.0" % Test,
  "org.typelevel" %% "scalacheck-effect" % "1.0.4" % Test,
  "org.typelevel" %% "cats-effect-testing-specs2" % "1.7.0" % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.7.0" % Test
)