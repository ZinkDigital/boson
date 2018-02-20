package io.zink.boson.json.jsonImpl

import java.util.concurrent.CompletableFuture

import io.zink.boson.json.Joson

class JosonFuse(first: Joson, second: Joson) extends Joson {
  /**
    * Apply this Joson to the String that arrives and at some point in the future complete
    * the future with the resulting String. In the case of an Extractor this will result in
    * the immutable String being returned unmodified.
    *
    * @param the Json string.
    * @return
    */
  override def go(jsonStr: String): CompletableFuture[String] = {
    val future: CompletableFuture[String] =
      CompletableFuture.supplyAsync(() => {
        val firstFuture: CompletableFuture[String] = first.go(jsonStr)
        second.go(firstFuture.join()).join()
      })
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
