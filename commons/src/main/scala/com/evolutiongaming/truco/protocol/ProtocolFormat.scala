package com.evolutiongaming.truco.protocol

import com.evolutiongaming.truco.model.{GameId, PlayerId}
import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec

trait ProtocolFormat {

  implicit val configuration: Configuration =
    Configuration.default
      .withDiscriminator("msg")
      .withSnakeCaseConstructorNames
      .withSnakeCaseMemberNames

  implicit val playerIdCodec: Codec[PlayerId] = deriveUnwrappedCodec
  implicit val gameIdCodec: Codec[GameId] = deriveUnwrappedCodec

}
