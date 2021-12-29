package com.evo.truco.server.lobby

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.evo.truco.model.PlayerId
import com.evo.truco.protocol.LobbyProtocol.LobbyRequest.{JoinQueue, LeaveLobby, LeaveQueue}
import com.evo.truco.protocol.LobbyProtocol.LobbyResponse
import com.evo.truco.protocol.LobbyProtocol.LobbyResponse.{GameCreated, WaitingCanceled, WaitingGame, Welcome}
import LobbyInput.JoinLobby
import com.evo.truco.model.{GameId, PlayerId}
import com.typesafe.scalalogging.LazyLogging

import java.util.UUID
import scala.collection.immutable.Queue

object Lobby extends LazyLogging {

  final case class LobbyState(clients: Map[PlayerId, ActorRef[LobbyResponse]], queue: Queue[PlayerId])

  def apply(): Behavior[LobbyCommand] =
    waiting(
      LobbyState(Map.empty, Queue.empty)
    )

  def waiting(state: LobbyState): Behavior[LobbyCommand] = {
    Behaviors.receive { (_, msg) =>
      msg match {
        case LobbyCommand(player, action) =>
          action match {
            case JoinLobby(actorRef) =>
              logger.info(s"User $player joined ...")

              actorRef ! Welcome

              waiting(state.copy(clients = state.clients + (player -> actorRef)))

            case JoinQueue =>
              logger.info(s"User $player joined queue...")
              state.clients.get(player).foreach(_ ! WaitingGame)

              val newQueue = state.queue.filterNot(_ == player).enqueue(player)

              newQueue.size match {
                case 1 =>
                  waiting(state.copy(queue = newQueue))
                case _ =>

                  val gameId = GameId(UUID.randomUUID().toString)
                  logger.info(s"Game started $gameId...")

                  val (player1, q1) = newQueue.dequeue
                  val (player2, finalQueue) = q1.dequeue

                  state.clients.get(player1).foreach(_ ! GameCreated(gameId))
                  state.clients.get(player2).foreach(_ ! GameCreated(gameId))

                  waiting(state.copy(queue = finalQueue))
              }

            case LeaveQueue =>

              logger.info(s"User $player left queue...")

              state.clients.get(player).foreach(_ ! WaitingCanceled)

              waiting(state.copy(queue = state.queue.filterNot(_ == player)))

            case LeaveLobby =>

              logger.info(s"User $player left lobby...")

              state.clients.get(player).foreach(_ ! WaitingCanceled)

              waiting(state.copy(clients = state.clients.filterNot(_._1 == player), queue = state.queue.filterNot(_ == player)))

            case _ =>

              Behaviors.same

          }
      }
    }
  }


}
