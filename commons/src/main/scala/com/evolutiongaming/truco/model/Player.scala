package com.evolutiongaming.truco.model

import cats.syntax.option._

final case class PlayerId(value: String) extends AnyVal

final case class Player(cards: Set[Card], options: Set[GameOption]) {

  def assignCards(c: Set[Card]): Option[Player] = copy(cards = c).some

  def assignOptions(o: Set[GameOption]): Player = copy(options = o)

  def playCard(c: Card): Option[Player] = if (cards.contains(c)) copy(cards = cards - c).some else None

}

object Player {
  def apply(): Player =
    Player(Set(), Set())

}