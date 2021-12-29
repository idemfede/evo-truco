package com.evolutiongaming.truco.server.lobby

import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.stream.{FlowShape, OverflowStrategy}
import com.evolutiongaming.truco.model.PlayerId
import com.evolutiongaming.truco.protocol.LobbyProtocol.LobbyRequest.LeaveLobby
import com.evolutiongaming.truco.protocol.LobbyProtocol.LobbyResponse.BadRequest
import com.evolutiongaming.truco.protocol.LobbyProtocol.{LobbyInput, LobbyRequest, LobbyResponse}
import com.evolutiongaming.truco.server.lobby.LobbyInput.{Failure, JoinLobby}
import com.typesafe.scalalogging.LazyLogging
import io.circe.Error
import io.circe.parser._
import io.circe.syntax._
import com.evolutiongaming.truco.protocol.LobbyProtocolFormat._

object LobbyFlow extends LazyLogging {

  def apply(sinkActor: ActorRef[LobbyCommand], connectionId: String) = {

    val sourceActor = ActorSource.actorRef[LobbyResponse](
      completionMatcher = Map.empty,
      failureMatcher = Map.empty,
      bufferSize = 100,
      overflowStrategy = OverflowStrategy.fail)

     Flow.fromGraph(GraphDSL.createGraph(sourceActor) { implicit builder =>
      sourceShape =>


        val materializedSource = builder.materializedValue.map(replyTo =>
          JoinLobby(replyTo))

        val mergeInput = builder.add(Merge[LobbyInput](2))

        val fromWebsocket = builder.add(Flow[Message].filter {
          case _: TextMessage => true
          case _: BinaryMessage => false
        })

        val parseMessages = builder.add(
          Flow[Message].collect {
            case TextMessage.Strict(msg) => decode[LobbyRequest](msg)
          }
        )

        val wrapAction = builder.add(Flow.fromFunction[LobbyInput, LobbyCommand](
          input => LobbyCommand(PlayerId(connectionId), input)
        ))

        val toWebSocket = builder.add(Flow[LobbyResponse].map {
          x => TextMessage(x.asJson.noSpaces)
        })

        val sink =
          ActorSink.actorRef[LobbyCommand](sinkActor,
            LobbyCommand(PlayerId(connectionId), LeaveLobby),
            ex => LobbyCommand(PlayerId(connectionId), Failure(ex)))

        val mergeResponse = builder.add(Merge[LobbyResponse](2))

        val parsedMessagesBroadcast = builder.add(Broadcast[Either[Error, LobbyRequest]](2))

        materializedSource ~> mergeInput ~> wrapAction ~> sink

        fromWebsocket ~> parseMessages ~> parsedMessagesBroadcast.in

        parsedMessagesBroadcast.out(0).collectType[Left[Error, LobbyRequest]].map(_ => BadRequest) ~> mergeResponse

        parsedMessagesBroadcast.out(1).collectType[Right[Error, LobbyRequest]].map(_.value) ~> mergeInput

        sourceShape ~> mergeResponse ~> toWebSocket

        FlowShape(fromWebsocket.in, toWebSocket.out)

    })

  }


}
