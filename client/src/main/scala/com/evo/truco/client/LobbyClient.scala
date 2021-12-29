package com.evo.truco.client

import cats.effect.{ExitCode, IO, IOApp}
import com.evo.truco.client.lobby.LobbyRouter
import com.evo.truco.client.effects.ConsoleOutput
import com.evo.truco.protocol.LobbyProtocol.LobbyResponse
import com.evo.truco.protocol.LobbyProtocolFormat.lobbyResponseCodec
import sttp.ws.WebSocket
import cats.syntax.all._
import com.evo.truco.client.config.Config
import GameClient.useGameWebSocket
import com.evo.truco.client.lobby.{LobbyRouter, LobbyService}
import com.evo.truco.client.effects.{ConsoleInput, ConsoleOutput, WebsocketResource}

object LobbyClient extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    Config.load[IO].flatMap {
      cfg =>
        WebsocketResource[IO].makeLobbyResource(cfg, useLobbyWebSocket)
          .useForever
          .as(ExitCode.Success)
    }
  }

  def useLobbyWebSocket(ws: WebSocket[IO]) = {

    val lobbyService = LobbyService[IO](ws)
    val lobbyRouter = LobbyRouter[IO](lobbyService)

    val producer = ConsoleInput[IO](lobbyRouter).repl
    val consumer = ConsoleOutput[IO, LobbyResponse](ws).repl

    (producer, consumer).parMapN { (_, _) => () }.handleErrorWith(_ => IO.unit)
  }


}
