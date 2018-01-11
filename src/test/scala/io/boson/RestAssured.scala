package io.boson

import javax.json.Json

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonImpl.BosonValidate
import io.restassured.module.scala.RestAssuredSupport.AddThenToResponse
import io.restassured.RestAssured.given
import io.restassured.RestAssured.when
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import okhttp3.mockwebserver.{MockResponse, MockWebServer}
import org.hamcrest.Matchers._
import org.junit.{After, Before, Test}
import org.junit.Assert.assertThat

class RestAssured {

  var webServer: MockWebServer = null

  val br4: BsonArray = new BsonArray().add("Insecticida")
  val br1: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius").add(br4)
  val obj1: BsonObject = new BsonObject().put("José", br1)
  val br2: BsonArray = new BsonArray().add("Spider")
  val obj2: BsonObject = new BsonObject().put("José", br2)
  val br3: BsonArray = new BsonArray().add("Fly")
  val obj3: BsonObject = new BsonObject().put("José", br3)
  val arr: BsonArray = new BsonArray().add(2.2).add(obj1).add(obj2).add(obj3).add(br4)
  val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)

  @Before
  def `Mock web server is initialized`(): Unit = {
    webServer = new MockWebServer
    webServer.start()
  }

  @After
  def `Mock web server is shutdown`(): Unit = {
    webServer.shutdown()
  }

  @Test
  def `trying out rest assured in scala`(): Unit = {
    val response = new MockResponse
    response.setBody(bsonEvent.toString)
    response.setHeader("content-type", "application/json")
    webServer.enqueue(response)

    val seq: Seq[Any] = Seq(Seq(2.2))

    val byteArray =new BsonObject(new JsonObject(
      given().port(webServer.getPort)
      .when().get("/greetBSON").asString()
    )).encodeToBarray()

    val boson = Boson.validate("StartUp.[0]", (out: Seq[Any]) => {
      assertThat(out, equalTo(seq))
    })

    boson.go(byteArray).join()

      //.Then().statusCode(200).body("StartUp[0]", equalTo(2.2f))
  }

}
