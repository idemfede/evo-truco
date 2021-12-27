package com.evolutiongaming.truco.client.config

import enumeratum.EnumEntry.Lowercase
import enumeratum.{CirisEnum, Enum, EnumEntry}

sealed abstract class AppEnvironment extends EnumEntry with Lowercase

object AppEnvironment extends Enum[AppEnvironment] with CirisEnum[AppEnvironment] {
  case object Local extends AppEnvironment

  case object Remote extends AppEnvironment

  val values = findValues
}
