package com.evo.truco.client.game

import cats.effect.kernel.Sync
import com.evo.truco.model.Card
import com.evo.truco.protocol.GameProtocol.GameRequest
import com.evo.truco.protocol.GameProtocol.GameRequest.{LeaveGame, PlayCard}
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import sttp.ws.WebSocket

trait GameService[F[_]] {

  def leaveGame: F[Unit]

  def playCard(card: String): F[Unit]

}


object GameService {

  import com.evo.truco.protocol.GameProtocolFormat.{cardDecoder, gameRequestCodec}


  def apply[F[_] : Sync](ws: WebSocket[F]) = new GameService[F] {
    override def leaveGame: F[Unit] = {
      val request: GameRequest = LeaveGame //TODO not encoding/decoding with configured
      ws.sendText(request.asJson.noSpaces)
    }

    override def playCard(card: String): F[Unit] = decode[Card](s"\"$card\"") match { //TODO wtf????
      case Left(_) => Sync[F].unit
      case Right(value) =>
        val request: GameRequest = PlayCard(value)
        ws.sendText(request.asJson.noSpaces)
    }
  }
}