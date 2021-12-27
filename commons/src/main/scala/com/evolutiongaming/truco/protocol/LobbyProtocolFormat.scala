package com.evolutiongaming.truco.protocol

import com.evolutiongaming.truco.protocol.LobbyProtocol.LobbyRequest.{JoinQueue, LeaveLobby}
import com.evolutiongaming.truco.protocol.LobbyProtocol.{LobbyRequest, LobbyResponse}
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.Codec

object LobbyProtocolFormat extends ProtocolFormat {

  implicit val lobbyResponseCodec: Codec[LobbyResponse] = deriveConfiguredCodec

  implicit val lobbyRequestCodec: Codec[LobbyRequest] = deriveConfiguredCodec

}
