package com.evolutiongaming.truco.model

sealed trait GameOption

object GameOption {
  case object PlayCard extends GameOption

  case object WaitOpponent extends GameOption

  val waitingOptions: Set[GameOption] = Set(WaitOpponent)

  val startingOptions: Set[GameOption] = Set(PlayCard)

  val noOptions: Set[GameOption] = Set()
}
