//package io.zink.boson
//
//import bsonLib.{BsonArray, BsonObject}
//import io.restassured.RestAssured.given
//import io.vertx.core.json.JsonObject
//import okhttp3.mockwebserver.{MockResponse, MockWebServer}
//import org.hamcrest.Matchers._
//import org.junit.Assert.assertThat
//import org.junit.{After, Before, Test}
//
//import scala.concurrent.{Await, Future}
//import scala.concurrent.duration.Duration
//
//class RestAssured {
//
//  var webServer: MockWebServer = null
//
//  val br4: BsonArray = new BsonArray().add("Insecticida")
//  val br1: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius").add(br4)
//  val obj1: BsonObject = new BsonObject().put("José", br1)
//  val br2: BsonArray = new BsonArray().add("Spider")
//  val obj2: BsonObject = new BsonObject().put("José", br2)
//  val br3: BsonArray = new BsonArray().add("Fly")
//  val obj3: BsonObject = new BsonObject().put("José", br3)
//  val arr: BsonArray = new BsonArray().add(2.2).add(obj1).add(obj2).add(obj3).add(br4)
//  val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
//
//  @Before
//  def `Mock web server is initialized`(): Unit = {
//    webServer = new MockWebServer
//    webServer.start()
//  }
//
//  @After
//  def `Mock web server is shutdown`(): Unit = {
//    webServer.shutdown()
//  }
//
//  @Test
//  def `trying out rest assured in scala`(): Unit = {
//    val response = new MockResponse
//    response.setBody(bsonEvent.toString)
//    response.setHeader("content-type", "application/json")
//    webServer.enqueue(response)
//
//    val seq: Seq[Any] = Vector(2.2)
//
//    val byteArray = new BsonObject(new JsonObject(
//      given().port(webServer.getPort)
//      .when().get("/greetBOSON").asString()
//    )).encodeToBarray()
//
//    val boson = Boson.validate("StartUp.[0]", (out: Seq[Any]) => {
//      assertThat(out, equalTo(seq))
//    })
//
//    val future: Future[Array[Byte]] = boson.go(byteArray)
//    val result: Array[Byte] = Await.result(future, Duration.Inf)
//  }
//
//}
