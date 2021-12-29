package com.evo.truco.server.game

import akka.actor.typed.ActorRef
import com.evo.truco.model.PlayerId
import com.evo.truco.protocol.GameProtocol.{GameInput, GameResponse}

case class GameCommand(playerId: PlayerId, action: GameInput)

object GameInput {

  final case class JoinGame(actorRef: ActorRef[GameResponse]) extends GameInput

  final case class Failure(ex: Throwable) extends GameInput

}