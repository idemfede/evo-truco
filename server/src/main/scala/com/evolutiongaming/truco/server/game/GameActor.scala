package com.evolutiongaming.truco.server.game


import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.evolutiongaming.truco.model.PlayerId
import com.evolutiongaming.truco.protocol.GameProtocol.GameRequest.{LeaveGame, PlayCard}
import com.evolutiongaming.truco.protocol.GameProtocol.GameResponse.{FullGameStatus, GameFinished, GameStarted, LostGame, OpponentLeft, WaitingForOpponent, WonGame}
import com.evolutiongaming.truco.server.game.GameInput.JoinGame
import com.typesafe.scalalogging.LazyLogging

object GameActor extends BroadCasting with LazyLogging {


  def apply(): Behavior[GameCommand] = {
    waitingForPlayers(
      GameState()
    )
  }

   def gameFinished(): Behavior[GameCommand] = {
    Behaviors.ignore
  }

  private def waitingForPlayers(state: GameState): Behavior[GameCommand] = {
    Behaviors.receive { (_, msg) =>
      msg match {
        case GameCommand(playerId, action) =>
          action match {
            case JoinGame(replyTo) =>

              logger.info(s"game Joined, current state $state")

              replyTo ! WaitingForOpponent

              state.players.size match {
                case 0 =>
                  state
                    .addPlayer(playerId, replyTo)
                    .fold[Behavior[GameCommand]] {
                      logger.info(s"Something went wrong")
                      broadcastInvalid(Seq(replyTo))
                      Behaviors.same
                    } {
                      waitingForPlayers
                    }
                case 1 =>
                  state
                    .addPlayer(playerId, replyTo)
                    .flatMap(_.startGame)
                    .fold[Behavior[GameCommand]] {
                      logger.info(s"Something went wrong")
                      broadcastInvalid(Seq(replyTo))
                      Behaviors.same
                    } {
                      newState => {
                        newState.clients.foreach(entry => entry._2 ! GameStarted(entry._1))
                        broadcast(FullGameStatus(newState.game, newState.players), newState.clients)
                        playingGame(newState)
                      }
                    }
                case _ =>
                  Behaviors.same
              }
            case LeaveGame =>
              playerLeft(state, playerId)

            case _ =>
              Behaviors.same
          }
      }
    }
  }

  private def playerLeft(state: GameState, playerId: PlayerId): Behavior[GameCommand] = {
    val newState = state.removePlayer(playerId)

    newState.clients.foreach(entry => entry._2 ! OpponentLeft)
    newState.clients.foreach(entry => entry._2 ! WaitingForOpponent)
    waitingForPlayers(newState)
  }

  private def playingGame(state: GameState): Behavior[GameCommand] = {
     Behaviors.receive { (_, msg) =>
      msg match {
        case GameCommand(playerId, action) =>
          action match {
            case PlayCard(card) =>

              logger.info(s"Card $card played by $playerId")

              val newState = for {
                playCard <- state.playCard(playerId, card)
                state <- (playCard.game.miniRoundComplete, playCard.game.availableMiniRounds) match {
                  case (true, true) => playCard.startMiniRound
                  case (true, false) => playCard.startRound
                  case (false, _) => playCard.continueMiniRound(playerId)
                }
              } yield state


              newState.fold[Behavior[GameCommand]] {
                logger.info(s"Something went wrong")
                broadcastInvalid(state.clients.values.toSeq)
                broadcast(FullGameStatus(state.game, state.players), state.clients)
                Behaviors.same
              } {
                 case newState if newState.game.scores.values.exists(_ >= 1) =>
                   val orderedScores = newState.game.scores.toSeq.sortBy(_._2)

                  for {
                    winner <- orderedScores.headOption
                    client <- newState.clients.get(winner._1)
                  } yield client ! WonGame

                  for {
                    loser <- orderedScores.lastOption
                    client <- newState.clients.get(loser._1)
                  } yield client ! LostGame

                  broadcast(GameFinished(newState.game), newState.clients)
                  gameFinished()
                case newState =>
                  broadcast(FullGameStatus(newState.game, newState.players), newState.clients)
                  playingGame(newState)
              }

            case LeaveGame =>
              playerLeft(state, playerId)
          }
      }
    }
  }

}
