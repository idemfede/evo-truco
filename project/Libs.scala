import sbt._

object Libs {

  val akkaVersion = "2.6.18"
  val akkaHttpVersion = "10.2.6"
  val scalaLoggingVersion = "3.9.4"
  val scalaTestVersion = "3.2.9"
  val logbackVersion = "1.2.9"
  val circeVersion = "0.14.1"
  val sttpVersion = "3.3.17"
  val catsEffectVersion = "3.3.0"
  val kindProjectorVersion = "0.13.2"
  val cirisVersion = "2.3.1"

  object Akka {
    val ActorTyped = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
    val StreamTyped = "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion
    val HttpCore = "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
    val Http = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

    val HttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
    val ActorTestKit = "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion
  }

  object Circe {
    val Core = "io.circe" %% "circe-core" % circeVersion
    val Generic = "io.circe" %% "circe-generic" % circeVersion
    val Parser = "io.circe" %% "circe-parser" % circeVersion
    val GenericExtras = "io.circe" %% "circe-generic-extras" % circeVersion
  }

  object Sttp {
    val Core = "com.softwaremill.sttp.client3" %% "core" % sttpVersion
    //    val BackendCats = "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % sttpVersion
    val BackendFs2 = "com.softwaremill.sttp.client3" %% "async-http-client-backend-fs2" % sttpVersion
  }

  object CompilerPlugin {
    val kindProjector = compilerPlugin(
      "org.typelevel" % "kind-projector" % kindProjectorVersion cross CrossVersion.full
    )
  }

  object Ciris {
    val Core = "is.cir" %% "ciris" % cirisVersion
    val Enum = "is.cir" %% "ciris-enumeratum" % cirisVersion
  }


  val Logging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
  val Logback = "ch.qos.logback" % "logback-classic" % logbackVersion
  val ScalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
  val CatsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion


}
