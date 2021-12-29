package com.evo.truco.model

import cats.syntax.option._

import scala.annotation.tailrec

//TODO implicit conversion
final case class GameId(value: String) extends AnyVal

final case class Game(scores: Map[PlayerId, Int],
                      board: Map[PlayerId, List[Card]]) {

  def addPlayer(playerId: PlayerId): Game = copy(scores + (playerId -> 0))

  def removePlayer(playerId: PlayerId): Game = copy(scores - playerId, board - playerId)

  def resetBoard: Option[Game] =
    copy(board = Map()).some

  def scorePoints(playerId: PlayerId, roundScore: Int): Option[Game] = {

    val score = scores.getOrElse(playerId, 0) + roundScore

    copy(scores = scores.updated(playerId, score)).some

  }


  def playCard(playerId: PlayerId, c: Card): Option[Game] = {

    val cards = board.getOrElse(playerId, List()) :+ c

    copy(board = board.updated(playerId, cards)).some

  }


  private def miniRoundWinners = {

    def nextMiniRoundWinner(currentBoard: Map[PlayerId, List[Card]]): Option[PlayerId] =
      currentBoard
        .map(x => (x._1, x._2.headOption.map(_.cardRanking)))
        .maxByOption(_._2)
        .fold(Option.empty[PlayerId]) {
          case (playerId, Some(_)) => Some(playerId)
          case _ => None
        }


    @tailrec
    def aux(currentBoard: Map[PlayerId, List[Card]], winners: Vector[PlayerId]): Vector[PlayerId] =
      nextMiniRoundWinner(currentBoard) match {
        case Some(x) => aux(currentBoard.map(x => (x._1, x._2.drop(1))), winners :+ x)
        case None => winners
      }


    aux(board, Vector())

  }


  def lastMiniRoundWinner: Option[PlayerId] = miniRoundWinners.lastOption

  def roundWinner: Option[PlayerId] = miniRoundWinners.groupBy(identity).maxByOption(_._2.size).map(_._1)

  //TODO constant
  def availableMiniRounds: Boolean = board.values.map(_.size).max < 3

  def miniRoundComplete: Boolean = {
    val playedCards = board.map(_._2.size)
    playedCards.size > 1 && playedCards.forall(_ == playedCards.head)
  }

}

object Game {
  def apply(): Game = Game(
    scores = Map(),
    board = Map()
  )
}

