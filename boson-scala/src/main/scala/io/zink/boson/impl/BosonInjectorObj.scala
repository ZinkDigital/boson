package io.zink.boson.impl

/*class BosonInjectorObj[T, R <: HList](expression: String,
                                      injectFunction: Option[T => T] = None,
                                      injectSeqFunction: Option[Seq[T] => T] = None
                                     )(implicit
                                       gen: LabelledGeneric.Aux[T, R],
                                       //inject: injectLabels[R], //TODO - Not Necessary???
                                       tp: Option[TypeCase[T]]) extends Boson {

  private val boson: BosonImpl = new BosonImpl()

  private val interpreter: Interpreter[T] = new Interpreter[T](boson,expression)

  private def runInterpreter(bsonEncoded: Either[Array[Byte],String]): Any =
    interpreter.run(bsonEncoded)

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] = Future{
      ???
    }
    future
  }

  override def go(bsonByteEncoding: String): Future[String] = {
    val future: Future[String] = Future {
      ???
    }
    future
  }

//  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)

}*/