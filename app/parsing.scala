package algorithms
import algorithms._

import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.time._

object parsing {
	import algorithms._ 

	case class Hashtag(indices: List[Int], text: String)
	implicit val hashtagReads = Json.reads[Hashtag]

	implicit val userReads = Json.reads[TwitterUser]

	case class BoundingBox(coordinates: List[List[List[Float]]])
	case class Place(bounding_box: BoundingBox, full_name: String)
	implicit val boxReads = Json.reads[BoundingBox]
	implicit val placeReads = Json.reads[Place]  

	def placeToLoc(p: Place): Location = {
		val box = p.bounding_box.coordinates(0)
		val lats = box.map(_(1))
		val longs = box.map(_(0))
		val avgLat = lats.sum / lats.length
		val avgLong = longs.sum / longs.length

		new Location(lat=avgLat, long=avgLong, name=p.full_name)
	}

	case class Media(url: String, kind: String)
	implicit val mediaReads: Reads[Media] = (
		(JsPath \ "media_url").read[String] and
		(JsPath \ "type").read[String]
	)(Media.apply _)

	def extractPictures(m: Option[List[Media]]): List[String] = m match {
		case Some(m) => m.filter(_.kind== "photo").map(_.url)
		case None => List()
	} 

	// example timestamp: Tue Mar 20 08:45:29 +0000 2018
	val timeFmt = java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss xxxx yyyy")

	implicit val tweetReads: Reads[Tweet] = (
		(JsPath \ "id").read[Long] and
		(JsPath \ "user").read[TwitterUser] and
		(JsPath \ "created_at").read[String].map(LocalDateTime.parse(_, timeFmt)) and
		(JsPath \ "entities" \ "hashtags").read[List[Hashtag]].map(_.map(_.text)) and
		(JsPath \ "text").read[String] and
		(JsPath \ "place").readNullable[Place].map(_.map(placeToLoc _)) and
		(JsPath \ "retweet_count").read[Int] and
		(JsPath \ "favorite_count").read[Int] and
		(JsPath \ "entities" \ "media").readNullable[List[Media]].map(extractPictures _)
	)(Tweet.apply _)

	def parseTweets(json: JsValue) = json.as[List[Tweet]]
}