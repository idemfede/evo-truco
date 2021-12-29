package com.evo.truco.protocol

import com.evo.truco.protocol.LobbyProtocol.{LobbyRequest, LobbyResponse}
import LobbyProtocol.LobbyRequest.{JoinQueue, LeaveLobby}
import LobbyProtocol.{LobbyRequest, LobbyResponse}
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.Codec

object LobbyProtocolFormat extends ProtocolFormat {

  implicit val lobbyResponseCodec: Codec[LobbyResponse] = deriveConfiguredCodec

  implicit val lobbyRequestCodec: Codec[LobbyRequest] = deriveConfiguredCodec

}
