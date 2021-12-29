package com.evo.truco.client

import cats.effect.{ExitCode, IO, IOApp}
import com.evo.truco.client.game.GameService
import com.evo.truco.client.effects.ConsoleOutput
import com.evo.truco.protocol.GameProtocol.GameResponse
import com.evo.truco.protocol.GameProtocolFormat.gameResponseCodec
import sttp.ws.WebSocket
import cats.syntax.all._
import com.evo.truco.client.game.{GameRouter, GameService}
import com.evo.truco.client.config.Config
import com.evo.truco.client.effects.{ConsoleInput, ConsoleOutput, WebsocketResource}

object GameClient extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    Config.load[IO].flatMap{
      cfg =>
        args.headOption.fold(
          IO.apply(ExitCode.Error)
        )(
          WebsocketResource[IO].makeGameResource(_, cfg, useGameWebSocket)
            .useForever
            .as(ExitCode.Success)
        )
    }
  }


  def useGameWebSocket(ws: WebSocket[IO]) = {

    val gameService = GameService[IO](ws)
    val gameRouter = GameRouter[IO](gameService)

    val producer = ConsoleInput[IO](gameRouter).repl
    val consumer = ConsoleOutput[IO, GameResponse](ws).repl

    (producer, consumer).parMapN { (_, _) => () }.handleErrorWith(_ => IO.unit)
  }


}
