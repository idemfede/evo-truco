package com.evolutiongaming.truco.client.config

import cats.effect.Async
import cats.syntax.all._
import ciris.env
import com.evolutiongaming.truco.client.config.AppEnvironment.{Local, Remote}

object Config {
  final case class AppConfig(host: String,
                             port: Int)

  def load[F[_] : Async]: F[AppConfig] =
    env("APP_ENV")
      .as[AppEnvironment]
      .flatMap {
        case Local =>
          (
            env("HOST").as[String].default("ws://localhost"),
            env("PORT").as[Int].default(8080)
            ).parMapN(AppConfig)
        case Remote =>
          (
            env("HOST").as[String].default("wss://evo-truco.herokuapp.com"),
            env("PORT").as[Int].default(443)
            ).parMapN(AppConfig)
      }
      .load[F]
}
