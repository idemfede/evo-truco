package com.evolutiongaming.truco.client.effects

import cats.syntax.all._
import com.evolutiongaming.truco.model.{Game, GameOption, Player}
import com.evolutiongaming.truco.protocol.GameProtocol
import com.evolutiongaming.truco.protocol.GameProtocol.{GameResponse, GameStatus}
import com.evolutiongaming.truco.protocol.LobbyProtocol.LobbyResponse

trait ConsoleTranslator[T] {
  def translate(t: T): Option[String]
}

object ConsoleTranslator {

  def apply[T: ConsoleTranslator]: ConsoleTranslator[T] = implicitly

  implicit def uglyLobbyTranslator[T] = new ConsoleTranslator[LobbyResponse] {
    override def translate(t: LobbyResponse): Option[String] = t match {
      case LobbyResponse.Welcome => "Welcome to Truco lobby !".some
      case LobbyResponse.BadRequest => "There was a problem processing your request".some
      case LobbyResponse.WaitingCanceled => "You left the waiting queue".some
      case LobbyResponse.WaitingGame => "Waiting for a game...".some
      case LobbyResponse.GameCreated(game) => s"You can join the game with ID ${game.value}".some
    }
  }

  implicit def veryUglyGameTranslator[T] = new ConsoleTranslator[GameResponse] {

    def toReadable(game: Game): String = {
      val board = game.board.map(_._2.map(card => card.rank.value + card.suit.shortName).mkString("[", " ", "]")).mkString("[", ", ", "]")
      s"Cards on board are $board"
    }

    def toReadable(player: Player) = {
      val cards = player.cards.map(card => card.rank.value + card.suit.shortName).mkString("[", ", ", "]")
      val options = player.options.map(readableOption).mkString("[", ", ", "]")
      s"Your cards are $cards and your options are $options"
    }

    def readableOption(option: GameOption): String = option match {
      case GameOption.PlayCard => "Play a card"
      case GameOption.WaitOpponent => "Wait for the opponent to play"
    }

    def readableScores(game: Game): String = game.scores.values.mkString("[", ", ", "]")

    def translateStatus(status: GameProtocol.GameStatus): Option[String] = status match {
      case GameResponse.WaitingForOpponent => "Waiting for opponent to join...".some
      case GameResponse.OpponentLeft => "Opponent has left the game".some
      case GameResponse.GameStarted(_) => "The game has started !!".some
      case GameResponse.PlayerStatus(game, player) => List(toReadable(game), toReadable(player)).mkString("", "\n", "").some
      case GameResponse.GameFinished(game) => s"The game has finished".some
      case GameResponse.WonGame => "You won :)".some
      case GameResponse.LostGame => "You lose :(".some
      case _ => None
    }

    def translateRestrictedStatus(status: GameProtocol.RestrictedGameStatus): Option[String] = status match {
      case GameResponse.FullGameStatus(_, _) => "Full game Status".some
      case _ => None
    }

    override def translate(t: GameResponse): Option[String] = t match {
      case status: GameProtocol.GameStatus => translateStatus(status)
      case status: GameProtocol.RestrictedGameStatus => translateRestrictedStatus(status)
      case GameResponse.BadRequest => "There was a problem processing your request".some
    }


  }
}