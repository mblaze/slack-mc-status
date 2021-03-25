name := "realm-status"

version := "0.1"

scalaVersion := "2.13.2"

libraryDependencies ++= Seq(
//  "dev.zio" %% "zio" % "1.0.0-RC19",
  "dev.zio" %% "zio-nio" % "1.0.0-RC6",
  "com.softwaremill.sttp.tapir" %% "tapir-core" % "0.15.0",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.15.0",
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % "0.15.0",
  "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % "2.1.1",
  "io.circe" %% "circe-generic" % "0.13.0",
  "org.scodec" %% "scodec-core" % "1.11.7",
)

