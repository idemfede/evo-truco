import com.evo.truco.model.{Card, Game, GameId, Player, PlayerId, Rank, Suit}
import com.evo.truco.model._
import com.evo.truco.protocol.GameProtocol.GameRequest.PlayCard
import com.evo.truco.protocol.GameProtocol.{GameRequest, GameResponse}
import com.evo.truco.protocol.LobbyProtocol.{LobbyRequest, LobbyResponse}

trait TestData {

  object LobbyData {

    val gameCreated = {
      val json =
        """
          |{
          |  "msg": "game_created",
          |  "game": "1234"
          |}""".stripMargin
      val data =
        LobbyResponse.GameCreated(
          game = GameId("1234")
        )
      (json, data)
    }

    val waitingGame = {
      val json =
        """
          |{
          |  "msg": "waiting_game"
          |}""".stripMargin

      (json, LobbyResponse.WaitingGame)
    }

    val waitingCanceled = {
      val json =
        """
          |{
          |  "msg": "waiting_canceled"
          |}""".stripMargin

      (json, LobbyResponse.WaitingCanceled)
    }

    val leaveLobby = {
      val json =
        """
          |{
          |  "msg": "leave"
          |}""".stripMargin

      (json, LobbyRequest.LeaveLobby)
    }
  }


  object GameData {
    val leaveGame = {
      val json =
        """
          |{
          |  "msg": "leave"
          |}""".stripMargin

      (json, GameRequest.LeaveGame)
    }

    val playCard = {
      val json =
        """
          |{
          |  "msg": "play_card",
          |  "card": "10o"
          |}""".stripMargin
      val data =
         PlayCard(
          card = Card(Rank(10), Suit.fromString("o"))
        )
      (json, data)
    }

    val waitingForOpponent = {
      val json =
        """
          |{
          |  "msg": "waiting_for_opponent"
          |}""".stripMargin

      (json, GameResponse.WaitingForOpponent)
    }

    val opponentLeft = {
      val json =
        """
          |{
          |  "msg": "opponent_left"
          |}""".stripMargin

      (json, GameResponse.OpponentLeft)
    }

    val gameStarted = {
      val json =
        """
          |{
          |  "msg": "game_started"
          |  "player": "1234",
          |}""".stripMargin
      val data =
        GameResponse.GameStarted(
          player = PlayerId("1234")
        )
      (json, data)
    }

    val emptyGame = {
      val json =
        """
          |{
          |  "scores": "{}"
          |  "board": "{}",
          |}""".stripMargin

      (json, Game())
    }

    val emptyPlayer = {
      val json =
        """
          |{
          |  "cards": "{}"
          |  "options": "{}",
          |}""".stripMargin

      (json, Player())
    }

    val emptyStatus = {
      val json =
        """
          |{
          |  "game" : {"scores" : {}, "board" : {}},
          |  "player" : {"cards" : [], "options" : []},
          |  "msg": "player_status"
          |}""".stripMargin
      val data =
        GameResponse.PlayerStatus(
          game = Game(),
          player = Player()
        )
      (json, data)
    }

    val emptyGameFinished: (String, GameResponse) = {
      val json =
        """
          |{
          |  "game" : {"scores" : {} ,"board" : {}},
          |  "msg" : "game_finished"
          |}""".stripMargin

      val data =
        GameResponse.GameFinished(
          game = Game()
        )
      (json, data)
    }

    val someCard = {
      val json = """"10o""""
      //        """
      //          |{
      //          |  "msg": "play_card",
      //          |  "card": "10o"
      //          |}""".stripMargin
      val data = Card(Rank(10), Suit.fromString("o"))
      (json, data)
    }

  }


}
