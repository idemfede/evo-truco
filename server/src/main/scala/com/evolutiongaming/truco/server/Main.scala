package com.evolutiongaming.truco.server

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

object Main extends App with LazyLogging {

  import system.executionContext

  implicit val system = ActorSystem[SpawnProtocol.Command](SpawnProtocol(), "guardian")

  val config = system.settings.config
  val host = config.getString("server.host")
  val port = config.getInt("server.port")

  new Server().start(host, port).onComplete {
    case Success(serverBinding) =>
      val localAddress = serverBinding.localAddress
      logger.info(s"Server started at ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(_) =>
      logger.error(s"Server failed to start")
      system.terminate()
  }

}
