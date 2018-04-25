package algorithms
import algorithms._

import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.time._

object parsing {
	case class Hashtag(indices: List[Int], text: String)
	implicit val hashtagReads: Reads[Hashtag] = Json.reads[Hashtag]
	case class Media(url: String, kind: String)
	implicit val mediaReads: Reads[Media] = (
		(JsPath \ "media_url").read[String] and
		(JsPath \ "type").read[String]
	)(Media.apply _)

	def coordsToLoc(c: Option[List[Float]]): algorithms.Location = {
		c match {
			case Some(List(lat, long)) => new algorithms.Location(lat=lat, long=long)
			case _  => algorithms.Location(lat=0, long=0) // default              
		}		
	}

	def extractPictures(m: Option[List[Media]]): List[String] = m match {
		case Some(m) => m.filter(_.kind== "photo").map(_.url)
		case None => List()
	} 

	// example timestamp: Tue Mar 20 08:45:29 +0000 2018
	val timeFmt = java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss xxxx yyyy")

	implicit val tweetReads: Reads[algorithms.Tweet] = (
		(JsPath \ "id").read[Long] and
		(JsPath \ "user" \ "screen_name").read[String] and
		(JsPath \ "user" \ "followers_count").read[Int] and
		(JsPath \ "created_at").read[String].map(LocalDateTime.parse(_, timeFmt)) and
		(JsPath \ "entities" \ "hashtags").read[List[Hashtag]].map(_.map(_.text)) and
		(JsPath \ "text").read[String] and
		(JsPath \\ "coordinates").readNullable[List[Float]].map(coordsToLoc _) and
		(JsPath \ "retweet_count").read[Int] and
		(JsPath \ "favorite_count").read[Int] and
		(JsPath \ "entities" \ "media").readNullable[List[Media]].map(extractPictures _) and
		(JsPath \ "user" \ "profile_image_url_https").read[String]
	)(algorithms.Tweet.apply _)

	def parseTweets(json: JsValue) = json.as[List[algorithms.Tweet]]
}