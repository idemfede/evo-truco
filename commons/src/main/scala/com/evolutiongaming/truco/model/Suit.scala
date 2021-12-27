package com.evolutiongaming.truco.model

import scala.language.implicitConversions

case class Suit(name: String, shortName: String)

object Suit {
  object Espada extends Suit("Espada", "e")

  object Copa extends Suit("Copa", "c")

  object Oro extends Suit("Oro", "o")

  object Basto extends Suit("Basto", "b")

  val all = List(Espada, Copa, Oro, Basto)

  implicit def fromString(s: String) = s match {
    case "e" => Espada
    case "c" => Copa
    case "o" => Oro
    case "b" => Basto
  }
}
