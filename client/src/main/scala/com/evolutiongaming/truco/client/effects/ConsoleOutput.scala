package com.evolutiongaming.truco.client.effects

import cats.Monad
import cats.effect.Sync
import cats.effect.std.Console
import cats.syntax.all._
import io.circe.Decoder
import io.circe.jawn.decode
import sttp.ws.WebSocket

trait ConsoleOutput[F[_]] {
  def repl: F[Unit]
}

object ConsoleOutput {

  def apply[F[_] : Sync : Monad : Console, T: ConsoleTranslator : Decoder](ws: WebSocket[F]): ConsoleOutput[F] =
    new ConsoleOutput[F] {
      override def repl: F[Unit] = {
        val loop = for {
          received <- ws.receiveText()
          msg <- Sync[F].delay(decode[T](received).toOption)
          translated <- Sync[F].delay(msg.flatMap(x => ConsoleTranslator[T].translate(x)))
          _ <- translated match {
            case Some(value) => Console[F].println(value) >> repl
            case None => Console[F].println(received) >> Sync[F].unit
          }
        } yield ()

        loop.handleErrorWith { error =>
          Console[F].println(error)

        }
      }
    }
}


