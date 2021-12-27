package com.evolutiongaming.truco.protocol

import com.evolutiongaming.truco.model._
import com.evolutiongaming.truco.protocol.GameProtocol.GameRequest.{LeaveGame, PlayCard}
import com.evolutiongaming.truco.protocol.GameProtocol.GameResponse.{FullGameStatus, PlayerStatus}
import com.evolutiongaming.truco.protocol.GameProtocol.{GameRequest, GameResponse, GameStatus}
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.extras.semiauto.{deriveConfiguredCodec, deriveConfiguredDecoder, deriveConfiguredEncoder, deriveEnumerationCodec}
import io.circe._

import scala.util.Try

object GameProtocolFormat extends ProtocolFormat with LazyLogging {

  implicit val playerScoresKeyEncoder: KeyEncoder[PlayerId] = playerId => playerId.value
  implicit val playerScoresKeyDecoder: KeyDecoder[PlayerId] = key => Some(PlayerId(key))

  implicit val playerScoresDecoder = Decoder.decodeMap[PlayerId, Int]
  implicit val playerScoresEncoder = Encoder.encodeMap[PlayerId, Int]

  private val pattern = "^([1-7]|10|11|12)\\s*([obec])$".r

  implicit val cardEncoder: Encoder[Card] = card => Json.fromString(card.rank.value + card.suit.shortName)
  implicit val cardDecoder: Decoder[Card] = Decoder.decodeString.emapTry { x =>
    Try {
      x match {
        case pattern(rank, suit) => Card(rank, suit)
        case _ => throw new RuntimeException("Invalid format")

      }
    }
  }

  implicit val gameCodec: Codec[Game] = deriveConfiguredCodec
  implicit val playerCodec: Codec[Player] = deriveConfiguredCodec
  implicit val gameOptionCodec: Codec[GameOption] = deriveEnumerationCodec

  implicit val gameResponseCodec: Codec[GameResponse] = deriveConfiguredCodec
  implicit val gameStatusCodec: Codec[GameStatus] = deriveConfiguredCodec
  implicit val fullStatusCodec: Codec[FullGameStatus] = deriveConfiguredCodec
  implicit val playerStatusCodec: Codec[PlayerStatus] = deriveConfiguredCodec

  implicit val gameRequestCodec: Codec[GameRequest] = deriveConfiguredCodec
}
