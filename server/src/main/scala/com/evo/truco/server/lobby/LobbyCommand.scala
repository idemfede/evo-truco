package com.evo.truco.server.lobby

import akka.actor.typed.ActorRef
import com.evo.truco.model.PlayerId
import com.evo.truco.protocol.LobbyProtocol.{LobbyInput, LobbyResponse}

case class LobbyCommand(playerId: PlayerId, action: LobbyInput)


object LobbyInput {

  final case class JoinLobby(actorRef: ActorRef[LobbyResponse]) extends LobbyInput

  final case class Failure(ex: Throwable) extends LobbyInput

}
