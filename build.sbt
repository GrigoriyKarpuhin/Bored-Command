import sbt.Keys.libraryDependencies
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "finalProject",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.4.8",
      "org.http4s" %% "http4s-blaze-client" % "0.23.14",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M7",
      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "com.lihaoyi" %% "ujson" % "3.0.0",
      "com.softwaremill.sttp.client4" %% "async-http-client-backend-cats" % "4.0.0-M7",
      "com.softwaremill.sttp.client4" %% "circe" % "4.0.0-M7",
      "org.tpolecat" %% "doobie-core"      % "1.0.0-RC4",
      "org.tpolecat" %% "doobie-hikari"    % "1.0.0-RC4",
      "io.estatico" %% "newtype" % "0.4.4",
      "com.github.pureconfig" %% "pureconfig" % "0.17.4",
    )
  )
