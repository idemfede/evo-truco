package com.evo.truco.server.game

import akka.actor.typed.ActorRef
import com.evo.truco.model.PlayerId
import com.evo.truco.protocol.GameProtocol.GameResponse
import com.evo.truco.protocol.GameProtocol.GameResponse.BadRequest

trait BroadCasting {

  def broadcast(message: GameResponse, players: Map[PlayerId, ActorRef[GameResponse]]): Unit = {
    players.foreach(_._2 ! message)
  }

  def broadcastInvalid(players: Seq[ActorRef[GameResponse]]): Unit = {
    players.foreach(_ ! BadRequest)
  }

}
