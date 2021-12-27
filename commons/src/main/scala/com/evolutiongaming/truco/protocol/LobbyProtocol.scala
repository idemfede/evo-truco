package com.evolutiongaming.truco.protocol

import com.evolutiongaming.truco.model.GameId

object LobbyProtocol {

  trait LobbyInput

  sealed trait LobbyRequest extends LobbyInput

  object LobbyRequest {

    final case object LeaveLobby extends LobbyRequest

    final case object JoinQueue extends LobbyRequest

    final case object LeaveQueue extends LobbyRequest

  }

  sealed trait LobbyResponse

  object LobbyResponse {

    final case object Welcome extends LobbyResponse

    final case object BadRequest extends LobbyResponse

    final case object WaitingCanceled extends LobbyResponse

    final case object WaitingGame extends LobbyResponse

    final case class GameCreated(game: GameId) extends LobbyResponse

   }


}
