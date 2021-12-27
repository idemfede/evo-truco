package com.evolutiongaming.truco.server.game

 import com.evolutiongaming.truco.model.{Card, Deck, Game, GameOption, Player, PlayerId}
 import akka.actor.typed.ActorRef
 import cats.implicits.catsSyntaxOptionId
 import com.evolutiongaming.truco.protocol.GameProtocol.GameResponse

 import scala.util.Random

final case class GameState(players: Map[PlayerId, Player],
                           clients: Map[PlayerId, ActorRef[GameResponse]],
                           game: Game) {

  def addPlayer(playerId: PlayerId, replyTo: ActorRef[GameResponse]): Option[GameState] =
    (players.get(playerId) match {
      case Some(_) => copy()
      case None => GameState(players + (playerId -> Player()), clients + (playerId -> replyTo), game.addPlayer(playerId))
    }).some

  def removePlayer(playerId: PlayerId): GameState =
    copy(players - playerId, clients - playerId, game = Game())

  def startGame: Option[GameState] = for {
    withDealtCards <- dealCards
    whoStarts <- Random.shuffle(players.keys).headOption
    withAssignedStart <- withDealtCards.shouldStart(whoStarts)
  } yield withAssignedStart

  def playCard(playerId: PlayerId, card: Card): Option[GameState] = for {
    player <- players.get(playerId)
    newPlayer <- player.playCard(card)
    newGame <- game.playCard(playerId, card)
  } yield GameState(players.updated(playerId, newPlayer), clients, newGame)


  def startMiniRound = for {
    nextPlayer <- game.lastMiniRoundWinner
    newMiniRound <- shouldStart(nextPlayer)
  } yield newMiniRound

  def startRound = for {
    nextPlayer <- game.roundWinner
    newScores <- scorePoints(nextPlayer, 1)
    newMiniRound <- newScores.shouldStart(nextPlayer)
    newRound <- newMiniRound.dealCards
  } yield newRound.copy(game = newRound.game.copy(scores = newRound.game.scores, board = Map())) // TODO make it better

  def continueMiniRound(lastPlayer: PlayerId) = for {
    nextPlayer <- nextPlayer(lastPlayer)
    newMiniRound <- shouldStart(nextPlayer)
  } yield newMiniRound

  private def scorePoints(playerId: PlayerId, roundScore: Int): Option[GameState] = for {
    newGame <- game.scorePoints(playerId, roundScore)
  } yield copy(game = newGame)

  private def shouldStart(shouldStart: PlayerId): Option[GameState] = {
    val options = players
      .map(x => (x._1, GameOption.waitingOptions))
      .updated(shouldStart, GameOption.startingOptions)

    val newPlayers = players.map(x => x._1 -> x._2.assignOptions(options.getOrElse(x._1, GameOption.noOptions)))

    copy(players = newPlayers).some
  }

  private def dealCards: Option[GameState] = Deck.shuffled().dealHands
    .map(
      x =>
        for {
          cardsByPlayer <- players.keys.zip(x)
          player <- players.get(cardsByPlayer._1)
          newPlayer <- player.assignCards(cardsByPlayer._2)
        } yield cardsByPlayer._1 -> newPlayer
    )
    .map(newPlayers => copy(players = newPlayers.toMap))

  private def nextPlayer(playerId: PlayerId): Option[PlayerId] = players.keys.filterNot(_ == playerId).headOption

}

object GameState {
  def apply(): GameState = GameState(Map(), Map(), Game())
}

