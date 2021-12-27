import Libs._

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.7"
ThisBuild / organization := "com.evolutiongaming"
ThisBuild / organizationName := "Evolution"

lazy val root = (project in file("."))
  .aggregate(
    commons,
    server,
    client,
  )
  .settings(
    name := "truco",
    inThisBuild(
      List(
        scalacOptions ++= Seq(
          "-deprecation",
          "-feature",
          "-language:higherKinds"
        ),
      )
    ),
  )

lazy val commons = (project in file("commons"))

  .settings(
    libraryDependencies ++= Seq(
      Circe.Core,
      Circe.Generic,
      Circe.GenericExtras,
      Circe.Parser,
      Logging,
      Logback,
      CompilerPlugin.kindProjector,
      ScalaTest % Test
    ),
  )

lazy val server = (project in file("server"))
  .dependsOn(
    commons % "compile->compile;test->test",
  )
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    Compile / mainClass := Some("com.evolutiongaming.truco.server.Main"),
    Docker / packageName := "truco",
    dockerBaseImage := "openjdk:11-jre-slim-buster",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts := Seq(),
    dockerUpdateLatest := true,
    libraryDependencies ++= Seq(
      Akka.ActorTyped,
      Akka.StreamTyped,
      Akka.HttpCore,
      Akka.Http,
      Akka.ActorTestKit % Test,
      Akka.HttpTestkit % Test,
      Circe.Core,
      Circe.Generic,
      Circe.GenericExtras,
      Circe.Parser,
      Logging,
      Logback % Runtime,
      ScalaTest % Test,
      CompilerPlugin.kindProjector
    ),
  )

lazy val client = (project in file("client"))
  .dependsOn(
    commons % "compile->compile;test->test",
  )
  .enablePlugins(JavaAppPackaging)
  .settings(
    libraryDependencies ++= Seq(
      Sttp.Core,
      Sttp.BackendFs2,
      CompilerPlugin.kindProjector,
      CatsEffect,
      Logging,
      Logback % Runtime,
    ),
  )

Compile / herokuAppName := "evo-truco"



