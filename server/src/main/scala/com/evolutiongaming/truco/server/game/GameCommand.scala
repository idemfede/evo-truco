package com.evolutiongaming.truco.server.game

import akka.actor.typed.ActorRef
import com.evolutiongaming.truco.model.PlayerId
import com.evolutiongaming.truco.protocol.GameProtocol.{GameInput, GameResponse}

case class GameCommand(playerId: PlayerId, action: GameInput)

object GameInput {

  final case class JoinGame(actorRef: ActorRef[GameResponse]) extends GameInput

  final case class Failure(ex: Throwable) extends GameInput

}