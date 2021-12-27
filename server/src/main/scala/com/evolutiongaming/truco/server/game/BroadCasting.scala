package com.evolutiongaming.truco.server.game


import akka.actor.typed.ActorRef
import com.evolutiongaming.truco.model.PlayerId
import com.evolutiongaming.truco.protocol.GameProtocol.GameResponse
import com.evolutiongaming.truco.protocol.GameProtocol.GameResponse.BadRequest

trait BroadCasting {

  def broadcast(message: GameResponse, players: Map[PlayerId, ActorRef[GameResponse]]): Unit = {
    players.foreach(_._2 ! message)
  }

  def broadcastInvalid(players: Seq[ActorRef[GameResponse]]): Unit = {
    players.foreach(_ ! BadRequest)
  }

}
