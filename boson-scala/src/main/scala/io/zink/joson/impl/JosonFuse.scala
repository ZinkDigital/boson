package io.zink.joson.impl

import io.zink.joson.Joson

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
class JosonFuse(first: Joson, second: Joson) extends Joson {
  /**
    * Apply this Joson to the String that arrives and at some point in the future complete
    * the future with the resulting String. In the case of an Extractor this will result in
    * the immutable String being returned unmodified.
    *
    * @param the Json string.
    * @return
    */
  override def go(jsonStr: String): Future[String] = {
    val future: Future[String] =
      Future{
        val firstFuture: Future[String] = first.go(jsonStr)
        firstFuture.value.get match{
          case Success(value) =>
            second.go(value).value.get match{
              case Success(secondValue)=>
                secondValue
              case Failure(e)=>
                jsonStr
            }
          case Failure(e) =>
            jsonStr
        }

      }
    future
  }


  /**
    * Fuse one Joson to another. The joson that is 'this' should be executed first before the
    * joson that is the parameter in teh case of update/read conflicts.
    * the immutable String being returned unmodified.
    *
    * @param the Joson to fuse to.
    * @return the fused Joson
    */
  override def fuse(joson: Joson): Joson = new JosonFuse(this, joson)
}
