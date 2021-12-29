package com.evolutiongaming.truco.server.game


import com.evolutiongaming.truco.model.{Player, PlayerId}
import com.typesafe.scalalogging.LazyLogging
import io.circe.Error
import io.circe.parser._
import io.circe.syntax._
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.{FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import com.evolutiongaming.truco.protocol.GameProtocol.GameRequest.LeaveGame
import com.evolutiongaming.truco.protocol.GameProtocol.GameResponse.{BadRequest, FullGameStatus, PlayerStatus}
import com.evolutiongaming.truco.protocol.GameProtocol.{GameInput, GameRequest, GameResponse, GameStatus}
import com.evolutiongaming.truco.protocol.GameProtocolFormat._
import com.evolutiongaming.truco.server.game.GameInput.{Failure, JoinGame}


object GameFlow extends LazyLogging  {

  def apply(sinkActor: ActorRef[GameCommand], connectionId: String) = {

    val responsesSource = ActorSource.actorRef[GameResponse](
      completionMatcher = Map.empty,
      failureMatcher = Map.empty,
      bufferSize = 100,
      overflowStrategy = OverflowStrategy.fail)

    Flow.fromGraph(GraphDSL.createGraph(responsesSource) { implicit builder =>
      sourceShape =>
        import GraphDSL.Implicits._

        val materializedSource = builder.materializedValue.map(replyTo =>
          JoinGame(replyTo))

        val mergeInput = builder.add(Merge[GameInput](2))

        val fromWebsocket = builder.add(Flow[Message].filter {
          case _: TextMessage => true
          case _: BinaryMessage => false
        })

        val parseMessages = builder.add(
          Flow[Message].collect {
            case TextMessage.Strict(msg) => decode[GameRequest](msg)
          }
        )

        val wrapAction = builder.add(Flow.fromFunction[GameInput, GameCommand](
          input => GameCommand(PlayerId(connectionId), input)
        ))

        val toWebSocket = builder.add(Flow[GameResponse].map {
          x => TextMessage(x.asJson.noSpaces)
        })


        val sink =
          ActorSink.actorRef[GameCommand](sinkActor,
            GameCommand(PlayerId(connectionId), LeaveGame),
            ex => GameCommand(PlayerId(connectionId), Failure(ex)))

        val filterRestrictedStatus = builder.add(
          Flow[FullGameStatus].collect(x => PlayerStatus(x.game, x.players.getOrElse(PlayerId(connectionId), Player())))
        )

        val mergeStatus = builder.add(Merge[GameResponse](3))

        val statusBroadcast = builder.add(Broadcast[GameResponse](2))

        val parsedMessagesBroadcast = builder.add(Broadcast[Either[Error, GameRequest]](2))

        materializedSource ~> mergeInput ~> wrapAction ~> sink

        fromWebsocket ~> parseMessages ~> parsedMessagesBroadcast.in

        parsedMessagesBroadcast.out(0).collectType[Left[Error, GameRequest]].map(_ => BadRequest) ~> mergeStatus
        parsedMessagesBroadcast.out(1).collectType[Right[Error, GameRequest]].map(_.value) ~> mergeInput

        sourceShape ~> statusBroadcast.in

        statusBroadcast.out(0).collectType[GameStatus] ~> mergeStatus ~> toWebSocket
        statusBroadcast.out(1).collectType[FullGameStatus] ~> filterRestrictedStatus ~> mergeStatus

        FlowShape(fromWebsocket.in, toWebSocket.out)

    })

  }


}
