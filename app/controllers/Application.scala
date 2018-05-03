package controllers

import algorithms._

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

import play.api.db._
import play.api.libs.json._

import java.io._
import java.sql._

object Application extends Controller {

	// Pages

	def index = Action { req =>
		if (req.cookies.get("user") == None) {
			Redirect("/signin")
		} 
		else {
	  	Ok(views.html.index())
	  }
	}

	def usersettings = Action { req =>
		req.cookies.get("user") match {
			case None => Redirect("/signin")
			case Some(c) => {
				val user_handle = c.value
				DB.withConnection{ implicit conn =>
					Ok(views.html.usersettings(
						getUserWithFollowing(getUserByHandle(user_handle))))
				}
			}
		}		
	}

	def algorithmsettings = Action { req =>
		req.cookies.get("user") match {
			case None => Redirect("/signin")
			case Some(c) => {
				var user_handle = c.value
				DB.withConnection{ implicit conn =>
					Ok(views.html.algorithmsettings(
						getWeighting(getUserByHandle(user_handle))))
				}
			} 
		}		
	}

	def signin = Action{
		Ok(views.html.signin())
	}

  // API

	def followUser = updateFollows("INSERT INTO usersFollowing (uid, following) VALUES (?, ?)")
	def unfollowUser = updateFollows("DELETE FROM usersFollowing WHERE uid = ? AND following = ?")
	def followHashtag = updateFollows("INSERT INTO hashtagsFollowing (uid, following) VALUES (?, ?)")
	def unfollowHashtag = updateFollows("DELETE FROM hashtagsFollowing WHERE uid = ? AND following = ?")



	def updateFollows(query: String) = Action { req =>
		req.cookies.get("user") match {
			case None => BadRequest("Not signed in")
			case Some(c) => {
				var user_handle = c.value
				DB.withConnection{ implicit conn =>
					val user = getUserByHandle(user_handle)
					val pstmt = conn.prepareStatement(query)
					pstmt.setInt(1, user.id)
					pstmt.setString(2, req.getQueryString("name").getOrElse(""))
					pstmt.executeUpdate()
				}
				Ok("")
			} 		
		}
	}

	def setLocation = Action { req => 
		req.cookies.get("user") match {
			case None => BadRequest("Not signed in")
			case Some(c) => {
				var user_handle = c.value
				(req.getQueryString("lat"), req.getQueryString("long")) match {
					case (Some(lat), Some(long)) => try {
						DB.withConnection{ implicit conn => 
							val pstmt = conn.prepareStatement(
								"UPDATE users SET "
								+ "lat = ?,"
								+ "long = ?"
								+ "WHERE handle = ?")
							pstmt.setFloat(1, lat.toFloat)
							pstmt.setFloat(2, long.toFloat)
							pstmt.setString(3, user_handle)
							pstmt.executeUpdate()
							Ok("")
						}
					}
					catch{
						case e: NumberFormatException => BadRequest("Bad number")
					}
					case _ => BadRequest("Missing parameter")
				}
			}
		}
	}

	def setAlgSettings = Action { req =>
		def getParam(key: String) = {
			req.getQueryString(key) match {
				case None => 1f
				case Some(str) => try { 
					math.max(0f, math.min(1f, str.toFloat))
				} catch {
					case e: NumberFormatException => 1f
				}
			}
		}

		req.cookies.get("user") match {
			case None => BadRequest("Not signed in")
			case Some(c) => {
				var user_handle = c.value
				
				DB.withConnection { implicit conn =>
					val pstmt = conn.prepareStatement(
						"UPDATE users SET "
						+ "setting_proximity = ?,"
						+ "setting_timeliness = ?,"
						+ "setting_hashtags = ?,"
						+ "setting_popularity = ?,"
						+ "setting_userpopularity = ?"
						+ "WHERE handle = ?")
					pstmt.setFloat(1, getParam("proximity"))
					pstmt.setFloat(2, getParam("timeliness"))
					pstmt.setFloat(3, getParam("hashtags"))
					pstmt.setFloat(4, getParam("tweetPopularity"))
					pstmt.setFloat(5, getParam("userPopularity"))
					pstmt.setString(6, user_handle)
					pstmt.executeUpdate()
				}

				// could update scores for each tweet here

				Ok("")
			} 	
		}	
	}
	
	implicit val locWrites = Json.writes[algorithms.Location]
	implicit val weightWrites = Json.writes[algorithms.Weighting]
	implicit val userWrites = Json.writes[algorithms.TwitterUser]
	val tweetWrites1 = Json.writes[algorithms.Tweet] // doesn't include scores

	implicit val tweetWrites = new Writes[algorithms.Tweet]{
		def writes(tweet: algorithms.Tweet) = {
			tweetWrites1.writes(tweet) ++
        Json.obj("score" -> tweet.score) ++
			  Json.obj("scores" -> tweet.scores)
		}
	}

	def getTweets = Action { req =>
		val filepath = if (req.getQueryString("small")==None){
			"Backend/data_formatted.json"
		}
		else {
			"Backend/data_small.json"
		}
		val stream = new FileInputStream(new File(filepath))
		val json = try { Json.parse(stream) } finally { stream.close() }

		val the_tweets = parsing.parseTweets(json)


		req.cookies.get("user") match {
			case None => BadRequest("Not signed in")
			case Some(c) => {
				var user_handle = c.value
				DB.withConnection { implicit conn =>
					val user = getUserWithFollowing(getUserByHandle(user_handle))
					val weights = getWeighting(user)
					val sorted = algorithms.sort(the_tweets, user, weights)

					Ok(Json.toJson(sorted.take(100))) 	
				}
			} 		
		}
	}


  // Helper functions

	def getUserByHandle(handle: String)(implicit conn: Connection) = {
		val stmt = conn.createStatement
		stmt.executeUpdate(
			"CREATE TABLE IF NOT EXISTS users ("
			+ "id SERIAL,"
			+ "name varchar DEFAULT '',"
			+ "handle varchar,"
			+ "password varchar DEFAULT 'hunter2',"
			+ "lat float DEFAULT 51.752022,"
			+ "long float DEFAULT -1.257677," // default is Oxford
			+ "setting_proximity float DEFAULT 1,"
			+ "setting_timeliness float DEFAULT 1,"
			+ "setting_hashtags float DEFAULT 1,"
			+ "setting_popularity float DEFAULT 1,"
			+ "setting_userpopularity float DEFAULT 1,"
			+ "setinng_userrelation float DEFAULT 1,"
			+ "PRIMARY KEY(id))");

		var pstmt = conn.prepareStatement("SELECT * FROM users WHERE handle = ?")
		pstmt.setString(1, handle)
		var rs = pstmt.executeQuery()
		if (!rs.next){
			pstmt = conn.prepareStatement("INSERT INTO users (handle) VALUES(?)")
			pstmt.setString(1, handle)
			pstmt.executeUpdate()

			pstmt = conn.prepareStatement("SELECT * FROM users WHERE handle = ?")
			pstmt.setString(1, handle)
			rs = pstmt.executeQuery()
			assert(rs.next)
		}

		new algorithms.User(
			id=rs.getInt("id"),
			name=rs.getString("name"),
			handle=handle,
			location=new algorithms.Location(
				lat=rs.getFloat("lat"),
				long=rs.getFloat("long")
			),
			usersFollowing=List(),
			hashtagsFollowing=List()
		)
	}

	def getWeighting(user: algorithms.User)(implicit conn: Connection) = {
		val pstmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")
		pstmt.setInt(1, user.id)
		val rs = pstmt.executeQuery()
		assert(rs.next)
		new algorithms.Weighting(
			proximity=rs.getFloat("setting_proximity"),
			timeliness=rs.getFloat("setting_timeliness"),
			hashtags=rs.getFloat("setting_hashtags"),
			popularity=rs.getFloat("setting_popularity"),
			user=rs.getFloat("setting_userpopularity"),
			userRelevance=rs.getFloat("setinng_userrelation")
		)
	}

	def getUserWithFollowing(user: algorithms.User)(implicit conn: Connection) = {
		createFollowingTables(conn)
		var pstmt = conn.prepareStatement("SELECT * FROM usersFollowing WHERE uid = ?")
		pstmt.setInt(1, user.id)
		var rs = pstmt.executeQuery()
		var res1 = List[String]()
		while(rs.next){
			res1 = rs.getString("following") :: res1
		}
		
		pstmt = conn.prepareStatement("SELECT * FROM hashtagsFollowing WHERE uid = ?")
		pstmt.setInt(1, user.id)
		rs = pstmt.executeQuery()
		var res2 = List[String]()
		while(rs.next){
			res2 = rs.getString("following") :: res2
		}

		user.copy(usersFollowing=res1, hashtagsFollowing=res2)

	}

	def createFollowingTables(conn: java.sql.Connection) = {
		val stmt = conn.createStatement
		stmt.executeUpdate(
			"CREATE TABLE IF NOT EXISTS usersFollowing ("
			+ "id SERIAL,"
			+ "uid integer REFERENCES users(id),"
			+ "following varchar,"
			+ "PRIMARY KEY(id))")
		stmt.executeUpdate(
			"CREATE TABLE IF NOT EXISTS hashtagsFollowing ("
			+ "id SERIAL,"
			+ "uid integer REFERENCES users(id),"
			+ "following varchar,"
			+ "PRIMARY KEY(id))")
	}
}
