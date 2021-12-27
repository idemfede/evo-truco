import com.evolutiongaming.truco.model.Card
import com.evolutiongaming.truco.protocol.GameProtocol.GameRequest.{LeaveGame, PlayCard}
import com.evolutiongaming.truco.protocol.GameProtocol._
import com.evolutiongaming.truco.protocol.LobbyProtocol.LobbyRequest
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameProtocolFormatSpec extends AnyWordSpec
  with OptionValues
  with Matchers
  with TestData {

  "GameProtocolFormat" should {
    "decode game request protocol" in {

      import com.evolutiongaming.truco.protocol.GameProtocolFormat.gameRequestCodec

      List(
        GameData.leaveGame,
        GameData.playCard
      ).map(values => decode[GameRequest](values._1).toOption.value shouldBe values._2)
    }

    "encode PlayCard protocol" in {

      import com.evolutiongaming.truco.protocol.GameProtocolFormat.gameRequestCodec

      val playCardData: GameRequest = GameData.playCard._2
      val playCardJson: String = GameData.playCard._1

      decode[GameRequest](playCardJson).toOption.value shouldBe playCardData

    }


    "encode game response protocol" in {
      import com.evolutiongaming.truco.protocol.GameProtocolFormat.gameResponseCodec
      List[(String, GameResponse)](
        GameData.waitingForOpponent,
        GameData.opponentLeft,
        GameData.emptyStatus,
        GameData.emptyGameFinished
      ).map(values => values._2.asJson shouldBe parse(values._1).toOption.value)
    }

    "decode card" in {
      import com.evolutiongaming.truco.protocol.GameProtocolFormat.cardDecoder
      List(
        GameData.someCard
      ).map(values => decode[Card](values._1).toOption.value shouldBe values._2)
    }

    "encode card" in {
      import com.evolutiongaming.truco.protocol.GameProtocolFormat.cardEncoder
      List(
        GameData.someCard
      ).map(values => values._2.asJson shouldBe parse(values._1).toOption.value)
    }

  }


}