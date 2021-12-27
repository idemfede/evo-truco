package com.evolutiongaming.truco.server

import akka.NotUsed
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, handleWebSocketMessages, path, _}
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.util.Timeout
import GameRoute.{RouteError, RouteFound}
import GameRouterCommand.FindGame
import com.evolutiongaming.truco.server.game.GameFlow
import com.evolutiongaming.truco.server.lobby.{Lobby, LobbyCommand, LobbyFlow}
import com.typesafe.scalalogging.LazyLogging

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt

class Server extends LazyLogging {

  def routes(lobby: ActorRef[LobbyCommand], gameRouter: ActorRef[GameRouterCommand])
            (implicit system: ActorSystem[SpawnProtocol.Command]): Route = {
    implicit val timeout = Timeout(3, TimeUnit.SECONDS)
    implicit val scheduler = system.scheduler

    Route.seal(
      get {
        pathEndOrSingleSlash {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello! from truco-server :)</h1>"))
        }
      } ~
        path("join") {
          get {
            handleWebSocketMessages(LobbyFlow(lobby, UUID.randomUUID().toString))
          }
        } ~
        path("play") {
          (get & parameter("gameId")) {
            gameId => {
              onSuccess(gameRouter.ask[GameRoute](FindGame(gameId, _))) {
                case RouteFound(route) => handleWebSocketMessages(GameFlow(route, UUID.randomUUID().toString))
                case RouteError => complete("Game not found")
              }
            }
          }
        } ~
        path("echo") {
          get {
            handleWebSocketMessages(
              Flow[Message].map {
                case TextMessage.Strict(txt) => TextMessage("Received: " + txt)
                case _ => TextMessage("Message type unsupported")
              })
          }
        }
    )


  }


  def start(host: String, port: Int)(implicit system: ActorSystem[SpawnProtocol.Command]) = {

    import system.executionContext
    implicit val timeout = Timeout(3, TimeUnit.SECONDS)
    implicit val scheduler = system.scheduler

    val supervisedRouter = Behaviors
      .supervise(GameRouter())
      .onFailure(SupervisorStrategy.restart)

    val supervisedLobby = Behaviors
      .supervise(Lobby())
      .onFailure(SupervisorStrategy.restart)

    for {
      lobby <- system.ask[ActorRef[LobbyCommand]](SpawnProtocol.Spawn(supervisedLobby, "lobby", Props.empty, _))
      gameRouter <- system.ask[ActorRef[GameRouterCommand]](SpawnProtocol.Spawn(supervisedRouter, "router", Props.empty, _))
      serverRoutes = routes(lobby, gameRouter)
      server <- Http().newServerAt(host, port).bind(serverRoutes)
    } yield server

  }
}

