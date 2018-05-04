package controllers

import algorithms._

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.Logger

import play.api.db._
import play.api.libs.json._

import java.io._
import java.sql._

object Application extends Controller {
	import algorithms._

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
					sqlUpdate(query, user.id, req.getQueryString("name").getOrElse(""))
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
							sqlUpdate(
								"UPDATE users SET "
								+ "lat = ?,"
								+ "long = ?"
								+ "WHERE handle = ?",
								lat.toFloat, long.toFloat, user_handle)
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
					sqlUpdate(
						"UPDATE users SET "
						+ "setting_proximity = ?,"
						+ "setting_timeliness = ?,"
						+ "setting_hashtags = ?,"
						+ "setting_popularity = ?,"
						+ "setting_userpopularity = ?,"
						+ "setting_userrelevance = ?"
						+ "WHERE handle = ?",
						getParam("proximity"), 
						getParam("timeliness"), 
						getParam("hashtags"), 
						getParam("tweetPopularity"), 
						getParam("userPopularity"), 
						getParam("userRelevance"), 
						user_handle)
				}

				// could update scores for each tweet here

				Ok("")
			} 	
		}	
	}
	
	implicit val locWrites = Json.writes[Location]
	implicit val weightWrites = Json.writes[Weighting]
	implicit val userWrites = Json.writes[TwitterUser]
	val tweetWrites1 = Json.writes[Tweet] // doesn't include scores

	implicit val tweetWrites = new Writes[Tweet]{
		def writes(tweet: Tweet) = {
			tweetWrites1.writes(tweet) ++
        Json.obj("score" -> tweet.score) ++
			  Json.obj("scores" -> tweet.scores)
		}
	}

	def getTweets = Action { req =>
		
		req.cookies.get("user") match {
			case None => BadRequest("Not signed in")
			case Some(c) => {
				var user_handle = c.value
				DB.withConnection { implicit conn =>
					val the_tweets = getTweetsFromDB(conn)

					val user = getUserWithFollowing(getUserByHandle(user_handle))
					val weights = getWeighting(user)
					val sorted = sort(the_tweets, user, weights).take(100)

					val withPics = sorted.map({ tw => 
						val rs = sqlQuery("SELECT * FROM media WHERE tid = ?", tw.id)
						var urls = List[String]()
						while (rs.next) {
							urls = rs.getString("url") :: urls
						}
						val tw2 = tw.copy(image_urls=urls)
						tw2.scores = tw.scores; tw2.score = tw.score

						tw2
					})

					Ok(Json.toJson(withPics)) 	
				}
			} 		
		}
	}

	def updateDB = Action { req =>
		DB.withConnection { implicit conn =>
			sqlUpdate(
				"CREATE TABLE IF NOT EXISTS tweets ("
				+ "id bigint,"
				+ "user_name varchar NOT NULL,"
				+ "user_handle varchar NOT NULL,"
				+ "user_followers integer NOT NULL,"
				+ "user_pic varchar NOT NULL,"
				+ "time timestamp NOT NULL,"
				+ "hashtags varchar NOT NULL,"
				+ "text varchar,"
				+ "lat float,"
				+ "long float,"
				+ "place_name varchar NOT NULL,"
				+ "retweets integer NOT NULL,"
				+ "favourites integer NOT NULL,"
				+ "PRIMARY KEY(id))")
			sqlUpdate(
				"CREATE TABLE IF NOT EXISTS media ("
				+ "id SERIAL,"
				+ "tid bigint REFERENCES tweets(id),"
				+ "url varchar NOT NULL,"
				+ "PRIMARY KEY(id))")

			val filepath = "Backend/data_formatted.json"
			val stream = new FileInputStream(new File(filepath))
			val json = try { Json.parse(stream) } finally { stream.close() }

			val the_tweets = parsing.parseTweets(json)
			var added = 0

			try {
				for (tw <- the_tweets){
					val rs = sqlQuery("SELECT '' FROM tweets WHERE id = ?", tw.id)
					if (!rs.next){
						sqlUpdate("INSERT INTO tweets VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
							tw.id, 
							tw.user.name, tw.user.screen_name, tw.user.followers_count, tw.user.profile_image_url_https,
							tw.timestamp,
							tw.hashtags.mkString(","),
							tw.text,
							tw.location.map(_.lat), tw.location.map(_.long), tw.location.map(_.name).getOrElse(""),
							tw.retweets, tw.favourites)
						for (u <- tw.image_urls){
							//Logger.debug(tid + " " + u)
							sqlUpdate("INSERT INTO media (tid, url) VALUES (?, ?)", tw.id, u)
						}
						added +=1
						if (added % 1000 == 0){
							Logger.info("Added " + added + " tweets")
						}
					}
				}
			}
			finally{
				Logger.info("Added " + added + " tweets")
			}

			Ok("Added " + added + " tweets")
		}
		
	}

	// Helper functions

	def getTweetsFromDB(implicit conn: Connection) = {
		val rs = sqlQuery("SELECT * FROM tweets")
		var tweets = List[Tweet]()
		while (rs.next) {
			val lat = rs.getFloat("lat")
			val loc = if(rs.wasNull) {None} else { // damn java's sql api is ugly
				Some(new Location(
					lat=lat,
					long=rs.getFloat("long"),
					name=rs.getString("place_name")))
			} 

			val tw = new Tweet (
				id=rs.getLong("id"),
				user=new TwitterUser (
					name=rs.getString("user_name"),
					screen_name=rs.getString("user_handle"),
					followers_count=rs.getInt("user_followers"),
					profile_image_url_https=rs.getString("user_pic")),
				timestamp=rs.getTimestamp("time").toLocalDateTime,
				text=rs.getString("text"),
				hashtags=rs.getString("hashtags").split(",").toList,
				location=loc,
				retweets=rs.getInt("retweets"),
				favourites=rs.getInt("favourites"),
				image_urls=List()
			) 

			tweets = tw :: tweets
		}

		tweets
	}

	def getUserByHandle(handle: String)(implicit conn: Connection) = {
		sqlUpdate(
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
			+ "setting_userrelevance float DEFAULT 1,"
			+ "PRIMARY KEY(id))");

		var rs = sqlQuery("SELECT * FROM users WHERE handle = ?", handle)
		if (!rs.next){
			sqlUpdate("INSERT INTO users (handle) VALUES(?)", handle)

			rs = sqlQuery("SELECT * FROM users WHERE handle = ?", handle)
			assert(rs.next)
		}

		new User(
			id=rs.getInt("id"),
			name=rs.getString("name"),
			handle=handle,
			location=new Location(
				lat=rs.getFloat("lat"),
				long=rs.getFloat("long")
			),
			usersFollowing=List(),
			hashtagsFollowing=List()
		)
	}

	def getWeighting(user: User)(implicit conn: Connection) = {
		val rs = sqlQuery("SELECT * FROM users WHERE id = ?", user.id)
		assert(rs.next)
		new Weighting(
			proximity=rs.getFloat("setting_proximity"),
			timeliness=rs.getFloat("setting_timeliness"),
			hashtags=rs.getFloat("setting_hashtags"),
			popularity=rs.getFloat("setting_popularity"),
			user=rs.getFloat("setting_userpopularity"),
			userRelevance=rs.getFloat("setting_userrelevance")
		)
	}

	def getUserWithFollowing(user: User)(implicit conn: Connection) = {
		createFollowingTables(conn)
		val rs1 = sqlQuery("SELECT * FROM usersFollowing WHERE uid = ?", user.id)
		var res1 = List[String]()
		while(rs1.next){
			res1 = rs1.getString("following") :: res1
		}
		
		val rs2 = sqlQuery("SELECT * FROM hashtagsFollowing WHERE uid = ?", user.id)
		var res2 = List[String]()
		while(rs2.next){
			res2 = rs2.getString("following") :: res2
		}

		user.copy(usersFollowing=res1, hashtagsFollowing=res2)

	}

	def createFollowingTables(implicit conn: Connection) = {
		sqlUpdate(
			"CREATE TABLE IF NOT EXISTS usersFollowing ("
			+ "id SERIAL,"
			+ "uid integer REFERENCES users(id),"
			+ "following varchar,"
			+ "PRIMARY KEY(id))")
		sqlUpdate(
			"CREATE TABLE IF NOT EXISTS hashtagsFollowing ("
			+ "id SERIAL,"
			+ "uid integer REFERENCES users(id),"
			+ "following varchar,"
			+ "PRIMARY KEY(id))")
	}

	def sqlStatement(q: String, params: Seq[Any])(implicit conn: Connection) = {
		val pstmt = conn.prepareStatement(q)
		var i=1

		def setParam(p: Any) {
			p match {
				case x: Int => pstmt.setInt(i, x)
				case x: Float => pstmt.setFloat(i, x)
				case x: String => pstmt.setString(i, x)
				case x: Long => pstmt.setLong(i, x)
				case x: java.time.LocalDateTime => pstmt.setTimestamp(i, Timestamp.valueOf(x))
				case Some(x) => setParam(x)
				case None => pstmt.setNull(i, Types.FLOAT) // the only null type we need - should be a more elegant way...
				case _ => throw new IllegalArgumentException("Unknown type for param " + i + " in query " + q)
			}
		}

		for (p <- params) {
			setParam(p)
			i+=1
		}

		pstmt
	}

	def sqlQuery(q: String, params: Any*)(implicit conn: Connection) = {sqlStatement(q, params).executeQuery()}
	def sqlUpdate(q: String, params: Any*)(implicit conn: Connection) = {sqlStatement(q, params).executeUpdate()}

}
