package com.evolutiongaming.truco.model

import scala.language.implicitConversions

final case class Rank(value: Int) extends AnyVal

object Rank {
  implicit def fromString(s: String): Rank = Rank(s.toInt)
}

final case class Card(rank: Rank, suit: Suit) {

  lazy val cardRanking: Int = (rank.value, suit) match {
    case (4, _) => 1
    case (5, _) => 2
    case (6, _) => 3
    case (7, Suit.Copa) => 4
    case (7, Suit.Basto) => 4
    case (10, _) => 5
    case (11, _) => 6
    case (12, _) => 7
    case (1, Suit.Copa) => 8
    case (1, Suit.Oro) => 8
    case (2, _) => 9
    case (3, _) => 10
    case (7, Suit.Oro) => 11
    case (7, Suit.Espada) => 12
    case (1, Suit.Basto) => 13
    case (1, Suit.Espada) => 14
    case _ => 0
  }
}




