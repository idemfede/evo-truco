package com.evolutiongaming.truco.protocol

import com.evolutiongaming.truco.model.{Card, Game, Player, PlayerId}

object GameProtocol {

  trait GameInput

  sealed trait GameRequest extends GameInput

  object GameRequest {

    final case class PlayCard(card: Card) extends GameRequest

    final case object LeaveGame extends GameRequest

  }

  sealed trait GameResponse

  sealed trait GameStatus extends GameResponse

  sealed trait RestrictedGameStatus extends GameResponse

  object GameResponse {

    final case object WaitingForOpponent extends GameStatus

    final case object OpponentLeft extends GameStatus

    final case class GameStarted(player: PlayerId) extends GameStatus

    final case class FullGameStatus(game: Game, players: Map[PlayerId, Player]) extends RestrictedGameStatus

    final case class PlayerStatus(game: Game, player: Player) extends GameStatus

    final case class GameFinished(game: Game) extends GameStatus

    final case object WonGame extends GameStatus

    final case object LostGame extends GameStatus

    final case object BadRequest extends GameResponse

  }

}
