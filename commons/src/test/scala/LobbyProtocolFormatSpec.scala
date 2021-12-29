import com.evo.truco.protocol.LobbyProtocol.{LobbyRequest, LobbyResponse}
import com.evo.truco.protocol.LobbyProtocolFormat._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LobbyProtocolFormatSpec extends AnyWordSpec
  with OptionValues
  with Matchers
  with TestData  {

  "LobbyProtocolFormat" should {
    "decode lobby input protocol" in {
      List(
        LobbyData.leaveLobby
      ).map(values => decode[LobbyRequest](values._1).toOption.value shouldBe values._2)
    }
    "encode lobby response protocol" in {
      List[(String, LobbyResponse)](
        LobbyData.gameCreated,
        LobbyData.waitingGame,
        LobbyData.waitingCanceled
      ).map(values => values._2.asJson shouldBe parse(values._1).toOption.value)
    }

  }


}