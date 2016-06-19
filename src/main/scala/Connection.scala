/**
 * Created by student on 17.06.16.
 */

package connection

import org.mongodb.scala._

trait Connection {
  //type C
  //def initialize: Unit
}

trait Savable {
  def +=(doc: Document) //: Observable[Completed]
  def close: Unit
}

class MongoConnection(private val host: String, private val port: Int = 27017) extends Savable {
  //type C = MongoClient
  require(host != null, "hostname must be specified")
  //val underlying: MongoClient = MongoClient("mongodb://" + host + ":" + port.toString)
  val underlying: MongoClient = MongoClient("mongodb://localhost:27017")
  val database: MongoDatabase = underlying.getDatabase("akka-mongo")
  val collection: MongoCollection[Document] = database.getCollection("streamdata")
  println("CONSTRUCTOR")

  def +=(doc: Document) = {
    println("SAVE CALLED")
    println(doc)
    collection.insertOne(doc).subscribe(new Observer[Completed] {
      override def onNext(result: Completed): Unit = println("INSERTED")

      override def onError(e: Throwable): Unit = println("Failed")

      override def onComplete(): Unit = println("COMPLETED")
    })
  }

  def close = underlying.close()

}

/*object MongoConnection{
  def apply(host: String, port: Int = 27017) = new MongoConnection(host, port)
}*/

