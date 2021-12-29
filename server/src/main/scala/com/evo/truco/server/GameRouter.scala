package com.evo.truco.server

import akka.actor.typed._
import akka.actor.typed.receptionist.Receptionist.Registered
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import GameRoute.{RouteError, RouteFound}
import GameRouterCommand.{FindGame, WrappedRoute, WrappedRouteError}
import com.evo.truco.server.game.{GameActor, GameCommand}
import com.evo.truco.server.game.GameActor
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.util.{Failure, Success}

sealed trait GameRouterCommand

object GameRouterCommand {
  final case class FindGame(gameId: String, replyTo: ActorRef[GameRoute]) extends GameRouterCommand

  final case class WrappedRoute(result: ActorRef[GameCommand], replyTo: ActorRef[GameRoute]) extends GameRouterCommand

  final case class WrappedRouteError(replyTo: ActorRef[GameRoute]) extends GameRouterCommand

}

sealed trait GameRoute

object GameRoute {
  final case class RouteFound(route: ActorRef[GameCommand]) extends GameRoute

  final case object RouteError extends GameRoute
}

object GameRouter extends LazyLogging {
  def apply()(implicit system: ActorSystem[SpawnProtocol.Command], timeout: Timeout, scheduler: Scheduler): Behavior[GameRouterCommand] =
    Behaviors.receive[GameRouterCommand] { (ctx, msg) =>
      msg match {
        case FindGame(gameId, replyTo) =>

          import system.executionContext

          val gameKey = ServiceKey[GameCommand](gameId)

          logger.info(s"Looking for game ${gameKey.toString}")

          val gameRoute: Future[ActorRef[GameCommand]] = for {
            listings <- system.receptionist.ask[Receptionist.Listing](Receptionist.Find(gameKey, _))
            newActor <- if (listings.serviceInstances(gameKey).isEmpty)
              for {
                actor <- system.ask[ActorRef[GameCommand]](SpawnProtocol.Spawn(GameActor(), gameId, Props.empty, _))
                _ <- system.receptionist.ask[Registered](Receptionist.Register(gameKey, actor, _))
              } yield actor
            else
              Future(listings.serviceInstances(gameKey).head)
          } yield
            newActor


          ctx.pipeToSelf(gameRoute) {
            case Success(route) => WrappedRoute(route, replyTo)
            case Failure(_) => WrappedRouteError(replyTo)
          }

          Behaviors.same

        case WrappedRoute(result, replyTo) =>

          replyTo ! RouteFound(result)

          Behaviors.same

        case WrappedRouteError(replyTo) =>

          replyTo ! RouteError

          Behaviors.same
      }

    }

}
