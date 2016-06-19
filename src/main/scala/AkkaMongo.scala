import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming.receiver.ActorHelper
import org.apache.spark.util.AkkaUtils
import akka.actor.{ ActorRef, ActorSystem, Props, Actor, ActorLogging }
import com.typesafe.config.ConfigFactory

import java.io.File
import scala.concurrent.duration._
import connection.MongoConnection
import org.mongodb.scala.Document
import org.apache.spark.rdd.RDD
import org.mongodb.scala.bson.BsonArray


case class Save(name: String)

class Saver extends Actor with ActorHelper{
	val conn = new MongoConnection("localhost", 27017)

	def receive = {
		case Save(name) =>
			//log.info(s"SAVER ACTOR CALLED with: $name")
			conn += Document("data" -> name)
			store(name)
		case _ =>
			println("SOMETHING ELSE CALLED IN SAVER")	
	}
}

object AkkaMongo{
	def main(args: Array[String]) = {
		val driverPort = 7777
    	val driverHost = "localhost"
    	val conf = new SparkConf(false) // skip loading external settings
      		.setMaster("local[*]") // run locally with as many threads as CPUs
      		.setAppName("Spark Streaming with Scala and Akka") // name in web UI
      		.set("spark.logConf", "true")
      		.set("spark.driver.port", driverPort.toString)
      		.set("spark.driver.host", driverHost)
      		.set("spark.akka.logLifecycleEvents", "true")
    	val ssc = new StreamingContext(conf, Seconds(2))
    	val actorName = "saver"
    	val actorStream = ssc.actorStream[String](Props[Saver], actorName)
    	actorStream.reduce(_ + " " + _).print()
    	ssc.start()

		java.util.concurrent.TimeUnit.SECONDS.sleep(3)

    	val actorSystem = SparkEnv.get.actorSystem
    	val url = s"akka.tcp://sparkDriver@$driverHost:$driverPort/user/Supervisor0/$actorName"
    	val saver = actorSystem.actorSelection(url)

		// Create the 'helloakka' actor system
  		//val system = ActorSystem("akkamongo")

  		// Create the 'greeter' actor
  		//val saver = system.actorOf(Props[Saver], "saver")


  		saver ! Save("John Doe")
  		saver ! Save("John John")
  		saver ! Save("John Susanna")
  		saver ! Save("John May")
  		saver ! Save("John Rio")

  		

  		//system.awaitTermination
  		scala.io.StdIn.readLine("Press Enter to stop Spark Streaming context and the application...")
  		ssc.stop(stopSparkContext = true, stopGracefully = true)
	}
}