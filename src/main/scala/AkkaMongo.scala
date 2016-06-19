import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming.receiver.ActorHelper
import org.apache.spark.util.AkkaUtils
import akka.actor.{ ActorRef, ActorSystem, Props, Actor, ActorLogging }
import com.typesafe.config.ConfigFactory

import java.io.File
import scala.io.Source
import scala.concurrent.duration._
import connection.MongoConnection
import loader.FileLoader
import org.mongodb.scala.Document
import org.apache.spark.rdd.RDD
import org.mongodb.scala.bson.BsonArray


case class Save(list: List[String])

class Saver extends Actor with ActorHelper{
	val conn = new MongoConnection("localhost", 27017)

	def receive = {
		case Save(list) =>
			//log.info(s"SAVER ACTOR CALLED with: $name")
			conn += Document("data" -> BsonArray(list))
			store(list)
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
    	val actorStream = ssc.actorStream[List[String]](Props[Saver], actorName)
    	//actorStream.reduce(_ + " " + _).print()
    	// placeholder for actual machine learning
    	actorStream.foreachRDD((rdd: RDD[List[String]]) => println(rdd.toString()))

    	// read csv file and prepare for streaming
    	val source: Source = FileLoader.open(new File(".").getCanonicalPath + "/src/main/scala/file/xmart.csv")
    	val lines: Iterator[String] = source.getLines()
    	// skip first 2 ; description and heading
    	val linesAsList = lines.drop(2).map(line => {
    	    line.split(",").toList(0) :: line.split(",").tail.map(_.replace(" ", "")).toList
    	})

    	ssc.start()

		java.util.concurrent.TimeUnit.SECONDS.sleep(3)

    	val actorSystem = SparkEnv.get.actorSystem
    	val url = s"akka.tcp://sparkDriver@$driverHost:$driverPort/user/Supervisor0/$actorName"
    	val saver = actorSystem.actorSelection(url)

    	linesAsList.foreach((line: List[String]) => {
    			saver ! Save(line)
    		})

  		scala.io.StdIn.readLine("Press Enter to stop Spark Streaming context and the application...")
  		ssc.stop(stopSparkContext = true, stopGracefully = true)
	}
}