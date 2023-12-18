import sbt.Keys.libraryDependencies
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "finalProject",
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2",
      "org.xerial" % "sqlite-jdbc" % "3.40.1.0",
      "org.typelevel" %% "cats-effect" % "3.4.8",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M7",
      "com.softwaremill.sttp.client4" %% "async-http-client-backend-cats" % "4.0.0-M7",
      "com.softwaremill.sttp.client4" %% "circe" % "4.0.0-M7",
      "com.lihaoyi" %% "ujson" % "3.0.0",
      "io.estatico" %% "newtype" % "0.4.4",
      "com.github.pureconfig" %% "pureconfig" % "0.17.4",
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "org.scalamock" %% "scalamock" % "5.2.0" % Test,
      "org.mockito" %% "mockito-scala" % "1.17.12",
      "org.mockito" %% "mockito-scala-scalatest" % "1.17.12" % Test,
      "org.mockito" %% "mockito-scala-cats" % "1.17.12" % Test
    )
  )

scalacOptions ++= Seq("-Ymacro-annotations")

enablePlugins(AssemblyPlugin)
assembly / assemblyJarName := "boredExec.jar"
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.discard
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
  case x => (assembly / assemblyMergeStrategy).value.apply(x)
}
