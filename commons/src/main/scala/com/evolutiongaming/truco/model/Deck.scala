package com.evolutiongaming.truco.model

import scala.util.{Random, Try}

final case class Deck(cards: List[Card]) {

  //TODO constant
  def dealHands: Option[List[Set[Card]]] = for {
    firstDeal <- deal(3)
    secondDeal <- firstDeal._2.deal(3)
  } yield List(firstDeal._1.toSet, secondDeal._1.toSet)

  private def deal(amount: Int): Option[(List[Card], Deck)] =
    Try(cards.splitAt(amount)).map(result => (result._1, Deck(result._2))).toOption
}

object Deck {

  //TODO constant
  def shuffled(): Deck = {
    val cards = for {
      suit <- Suit.all
      rank <- (1 to 7) ++ (10 to 12)
    } yield Card(Rank(rank), suit)

    Deck(Random.shuffle(cards))
  }

}