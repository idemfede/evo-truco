package com.evolutiongaming.truco.client.clientGame

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.effect.Async
import cats.effect.kernel.Sync
import cats.syntax.all._

object GameRouter {
  def apply[F[_] : Monad : Async : GameService](s: GameService[F]): Kleisli[OptionT[F, *], List[String], Unit] =
    Kleisli[OptionT[F, *], List[String], Unit] {
      case "card" :: card :: Nil => OptionT.liftF(
        for {
          _ <- Sync[F].delay(println(card))
          _ <- s.playCard(card)
        } yield ()
      )
      case "leaveGame" :: Nil => OptionT.liftF(
        for {
          _ <- s.leaveGame
        } yield ()
      )
      case _ => OptionT.none
    }
}
