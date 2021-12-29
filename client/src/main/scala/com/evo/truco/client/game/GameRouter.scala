package com.evo.truco.client.game

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.effect.Async
import cats.effect.kernel.Sync
import cats.syntax.all._

object GameRouter {
  def apply[F[_] : Monad : Async](s: GameService[F]): Kleisli[OptionT[F, *], List[String], Unit] =
    Kleisli[OptionT[F, *], List[String], Unit] {
      case "card" :: card :: Nil => OptionT.liftF(
        s.playCard(card).void
      )
      case "leave" :: Nil => OptionT.liftF(
        s.leaveGame.void
      )
      case _ => OptionT.none
    }
}
