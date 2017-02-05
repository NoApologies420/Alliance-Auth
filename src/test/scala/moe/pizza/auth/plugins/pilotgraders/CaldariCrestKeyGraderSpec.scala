package moe.pizza.auth.plugins.pilotgraders

import moe.pizza.auth.models.Pilot
import moe.pizza.crestapi.CrestApi
import moe.pizza.crestapi.CrestApi.{CallbackResponse, VerifyResponse}
import moe.pizza.eveapi.{EVEAPI, XMLApiResponse}
import moe.pizza.eveapi.endpoints.Character
import org.http4s.client.blaze.PooledHttp1Client
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

import scalaz.concurrent.Task

class CaldariCrestKeyGraderSpec
    extends WordSpec
    with MustMatchers
    with MockitoSugar {

  implicit val client = PooledHttp1Client() //TODO: no mocking?

  "CaldariCrestKeyGrader" when {
    "grading" should {
      "grade pilots who are caldari as THE ENEMY" in {
        val crest = mock[CrestApi]
        val eveapi = mock[EVEAPI]
        val char = mock[Character]
        when(eveapi.char).thenReturn(char)
        when(crest.refresh("REF")).thenReturn(Task {
          new CallbackResponse("access", "type", 1000, Some("refresh"))
        })
        when(crest.verify("access")).thenReturn(Task {
          new VerifyResponse(1, "Bob", "", "", "", "", "")
        })
        val pilotInfo =
          mock[moe.pizza.eveapi.generated.char.CharacterInfo.Result]
        when(pilotInfo.race).thenReturn("Caldari")
        when(char.CharacterInfo(1)).thenReturn(Task {
          new XMLApiResponse(DateTime.now(), DateTime.now(), pilotInfo)
        })
        val iwpg = new CaldariCrestKeyGrader(crest, eve = Some(eveapi))
        val p = new Pilot("bob",
                          Pilot.Status.unclassified,
                          "boballiance",
                          "bobcorp",
                          "Bob",
                          "none@none",
                          Pilot.OM.createObjectNode(),
                          List.empty[String],
                          List("1:REF"),
                          List.empty[String])
        iwpg.grade(p) must equal(Pilot.Status.banned)
        verify(crest).refresh("REF")
        verify(crest).verify("access")
        verify(char).CharacterInfo(1)
        verify(pilotInfo).race
      }
      "grade pilots who are not caldari as not THE ENEMY" in {
        val crest = mock[CrestApi]
        val eveapi = mock[EVEAPI]
        val char = mock[Character]
        when(eveapi.char).thenReturn(char)
        when(crest.refresh("REF")).thenReturn(Task {
          new CallbackResponse("access", "type", 1000, Some("refresh"))
        })
        when(crest.verify("access")).thenReturn(Task {
          new VerifyResponse(1, "Bob", "", "", "", "", "")
        })
        val pilotInfo =
          mock[moe.pizza.eveapi.generated.char.CharacterInfo.Result]
        when(pilotInfo.race).thenReturn("Gallente")
        when(char.CharacterInfo(1)).thenReturn(Task {
          new XMLApiResponse(DateTime.now(), DateTime.now(), pilotInfo)
        })
        val iwpg = new CaldariCrestKeyGrader(crest, eve = Some(eveapi))
        val p = new Pilot("bob",
                          Pilot.Status.unclassified,
                          "boballiance",
                          "bobcorp",
                          "Bob",
                          "none@none",
                          Pilot.OM.createObjectNode(),
                          List.empty[String],
                          List("1:REF"),
                          List.empty[String])
        iwpg.grade(p) must equal(Pilot.Status.unclassified)
        verify(crest).refresh("REF")
        verify(crest).verify("access")
        verify(char).CharacterInfo(1)
        verify(pilotInfo).race
      }
    }
  }

}
