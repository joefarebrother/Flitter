package controllers

import algorithms._

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

import play.api.db._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def usersettings = Action {
  	val user_handle = "user1" // placeholder
  	DB.withConnection{conn =>
  		Ok(views.html.usersettings(
  			getUserWithFollowing(conn, getUserByHandle(conn, user_handle))))
  	}
    
  }

  def algorithmsettings = Action {
  	val user_handle = "user1" // placeholder
  	DB.withConnection{conn =>
  		Ok(views.html.algorithmsettings(
  			getWeighting(conn, getUserByHandle(conn, user_handle))))
  	}
    
  }

  def followUser = updateFollows("INSERT INTO usersFollowing (uid, following) VALUES (?, ?)")

  def unfollowUser = updateFollows("DELETE FROM usersFollowing WHERE uid = ? AND following = ?")

  def followHashtag = updateFollows("INSERT INTO hashtagsFollowing (uid, following) VALUES (?, ?)")

	def unfollowHashtag = updateFollows("DELETE FROM hashtagsFollowing WHERE uid = ? AND following = ?")



  def updateFollows(query: String) = Action {req =>
  	val user_handle = "user1" // placeholder
  	DB.withConnection{conn =>
  		val user = getUserByHandle(conn, user_handle)
  		val pstmt = conn.prepareStatement(query)
  		pstmt.setInt(1, user.id)
  		pstmt.setString(2, req.getQueryString("name").getOrElse(""))
  		pstmt.executeUpdate()
  	}
  	Ok("")
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

  	val user_handle = "user1" // placeholder
  	DB.withConnection { conn =>
  		val pstmt = conn.prepareStatement(
  			"UPDATE users SET "
  			+ "setting_proximity = ?,"
  			+ "setting_timeliness = ?,"
  			+ "setting_hashtags = ?,"
  			+ "setting_popularity = ?"
  			+ "WHERE handle = ?")
  		pstmt.setFloat(1, getParam("proximity"))
  		pstmt.setFloat(2, getParam("timeliness"))
  		pstmt.setFloat(3, getParam("hashtags"))
  		pstmt.setFloat(4, getParam("popularity"))
  		pstmt.setString(5, user_handle)
  		pstmt.executeUpdate()
  	}

  	// could update scores for each tweet here

  	Ok("")
  		
  }

  def getUserByHandle(conn: java.sql.Connection, handle: String) = {
  	val stmt = conn.createStatement
  	stmt.executeUpdate(
  		"CREATE TABLE IF NOT EXISTS users ("
  	  + "id SERIAL,"
  	  + "name varchar DEFAULT '',"
      + "handle varchar,"
      + "lat float DEFAULT 51.752022,"
      + "long float DEFAULT -1.257677," // default is Oxford
      + "setting_proximity float DEFAULT 1,"
      + "setting_timeliness float DEFAULT 1,"
      + "setting_hashtags float DEFAULT 1,"
      + "setting_popularity float DEFAULT 1,"
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

  def getWeighting(conn: java.sql.Connection, user: algorithms.User) = {
  	val pstmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")
  	pstmt.setInt(1, user.id)
  	val rs = pstmt.executeQuery()
  	assert(rs.next)
  	new algorithms.Weighting(
  		proximity=rs.getFloat("setting_proximity"),
  		timeliness=rs.getFloat("setting_timeliness"),
  		hashtags=rs.getFloat("setting_hashtags"),
  		popularity=rs.getFloat("setting_popularity")
  	)
  }

  def getUserWithFollowing(conn: java.sql.Connection, user: algorithms.User) = {
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
