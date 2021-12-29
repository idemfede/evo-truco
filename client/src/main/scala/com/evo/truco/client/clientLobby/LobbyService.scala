package com.evo.truco.client.clientLobby

import com.evo.truco.protocol.LobbyProtocol.LobbyRequest
import com.evo.truco.protocol.LobbyProtocol.LobbyRequest.{JoinQueue, LeaveQueue}
import io.circe.syntax.EncoderOps
import sttp.ws.WebSocket

trait LobbyService[F[_]] {

  def leaveQueue: F[Unit]

  def joinQueue: F[Unit]

}

object LobbyService {

  import com.evo.truco.protocol.LobbyProtocolFormat.lobbyRequestCodec

  def apply[F[_]](ws: WebSocket[F]) = new LobbyService[F] {
    override def leaveQueue: F[Unit] = {
      val request: LobbyRequest = LeaveQueue //TODO not encoding/decoding with configured
      ws.sendText(request.asJson.noSpaces)
    }

    override def joinQueue: F[Unit] = {
      val request: LobbyRequest = JoinQueue //TODO not encoding/decoding with configured
      ws.sendText(request.asJson.noSpaces)
    }
  }
}