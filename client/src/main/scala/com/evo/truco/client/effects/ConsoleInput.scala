package com.evo.truco.client.effects

import cats.MonadThrow
import cats.data.{Kleisli, OptionT}
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.syntax.all._

trait ConsoleInput[F[_]] {
  def repl: F[Unit]
}

object ConsoleInput {

  def apply[F[_] : Sync: MonadThrow : Console](router: Kleisli[OptionT[F, *], List[String], Unit]): ConsoleInput[F] =
    new ConsoleInput[F] {
      override def repl: F[Unit] = {
        val loop = for {
          line <- Console[F].readLine
          args = line.split(" ").toList
          results <- router(args).value
          _ <- results match {
            case Some(_) =>  Sync[F].unit //// command accepted ///
            case None =>  Console[F].println("There was a problem processing your request")
          }
          _ <- repl
        } yield ()

        loop.handleErrorWith { error =>
          Console[F].println(error)
        }
      }
    }
}
