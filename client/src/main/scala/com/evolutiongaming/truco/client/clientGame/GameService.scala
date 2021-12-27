package com.evolutiongaming.truco.client.clientGame

import cats.effect.kernel.Sync
import com.evolutiongaming.truco.model.Card
import com.evolutiongaming.truco.protocol.GameProtocol.GameRequest
import com.evolutiongaming.truco.protocol.GameProtocol.GameRequest.{LeaveGame, PlayCard}
import io.circe.jawn.decode
import io.circe.syntax.EncoderOps
import sttp.ws.WebSocket

trait GameService[F[_]] {

  def leaveGame: F[Unit]

  def playCard(card: String): F[Unit]

}


object GameService {

  import com.evolutiongaming.truco.protocol.GameProtocolFormat.{cardDecoder, gameRequestCodec}


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