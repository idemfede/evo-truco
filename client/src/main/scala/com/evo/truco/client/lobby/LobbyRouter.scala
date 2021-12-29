package com.evo.truco.client.lobby

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.effect.Async
import cats.syntax.all._

object LobbyRouter {
  def apply[F[_] : Monad : Async](s: LobbyService[F]): Kleisli[OptionT[F, *], List[String], Unit] =
    Kleisli[OptionT[F, *], List[String], Unit] {
      case "leave" :: Nil => OptionT.liftF(
        s.leaveQueue.void
      )
      case "join" :: Nil => OptionT.liftF(
        s.joinQueue.void
      )
      case _ => OptionT.none
    }
}
