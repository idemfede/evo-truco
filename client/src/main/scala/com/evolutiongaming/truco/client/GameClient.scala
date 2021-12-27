package com.evolutiongaming.truco.client

import cats.effect.{ExitCode, IO, IOApp}
import com.evolutiongaming.truco.client.clientGame.{GameRouter, GameService}
import com.evolutiongaming.truco.client.effects.{ConsoleInput, ConsoleOutput, WebsocketResource}
import com.evolutiongaming.truco.protocol.GameProtocol.GameResponse
import com.evolutiongaming.truco.protocol.GameProtocolFormat.gameResponseCodec
import sttp.ws.WebSocket
import cats.syntax.all._

object GameClient extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    args.headOption.fold(
      IO.apply(ExitCode.Error)
    )(
      WebsocketResource[IO].makeGameResource(_, useGameWebSocket)
        .useForever
        .as(ExitCode.Success)
    )
  }


  def useGameWebSocket(ws: WebSocket[IO]) = {

    val gameService = GameService[IO](ws)
    val gameRouter = GameRouter[IO](gameService)

    val producer = ConsoleInput[IO](gameRouter).repl
    val consumer = ConsoleOutput[IO, GameResponse](ws).repl

    (producer, consumer).parMapN { (_, _) => () }.handleErrorWith(_ => IO.unit)
  }


}
