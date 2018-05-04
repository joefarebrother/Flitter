
package algorithms

import scala.List
import scala.math._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import scala.concurrent.duration._
import scala.math

object algorithms {
  case class Location(lat: Float, long: Float, name: String = "")

  /***** Tweets *****/
  
  case class Tweet(id: Long, 
                   user: TwitterUser,
                   timestamp: LocalDateTime, 
                   hashtags: List[String], 
                   text: String, 
                   location: Option[Location], 
                   retweets: Int, 
                   favourites: Int,
                   image_urls: List[String])
  {
    var score = 0: Float;
    var scores = null: Weighting;
  }
  // A Tweet object will represent a tweet and all its data
  
  /***** Users *****/
  
  case class User(id: Int,
                  name: String,
                  handle: String,
                  location: Location,
                  usersFollowing: List[String], // just their handles (screen names)
                  hashtagsFollowing: List[String]) 
  // A User object will represent a single user of the system and all their data

  // all these are named the same as their corresponding JS properties for easier parsing
  case class TwitterUser(name: String, 
                         screen_name: String,
                         followers_count: Int,
                         profile_image_url_https: String)
  
  
  /***** Measures of Relevance *****/
  
  abstract class Measure(me: User)
  // Each subclass of 'Measure' will define a measure of relevance for tweets
  // Relevance will be measured from the perspective of the user 'me'
  {
    def measure(tweet: Tweet): Float // Takes a tweet and returns a value between 0 and 10
    
    protected def validate(value: Float) = 
    // Checks that a value is between 0 and 10
    {
      assert (value >= 0);
      assert (value <= 10);
    }
  }
  
  class Proximity(me: User) extends Measure(me)
  // Measures relevance by proximity
  {
    def dist(x: Location, y: Location) : Float = {
      // Longtitude and Latitude are measured in degrees, but we want radians to do trig with
      def rad = toRadians _ // shorter

      // We use the haversine distance, as our location is given as longitude and latitudes
      val avgLongitude = (x.long - y.long) / 2.0
      val avgLatitude = (x.lat - y.lat) / 2.0
      // R is Earth's radius, meters 
      val R = 6371.0 * 1000.0
      
      // a = sin^2(avgLong) + cos(x.long) * cos(y.long) * sin^2(avgLat)
      val a = {pow(sin(rad(avgLongitude)), 2.0) +
              cos(rad(x.long))*cos(rad(y.long))*pow(sin(rad(avgLatitude)), 2.0)}
      // c = 2 * asin(sqrt(a))
      val c = 2.0 * scala.math.asin(scala.math.sqrt(a))
      // d = R * c
      return (R * c).toFloat
    }
    
    def measure(tweet: Tweet): Float = {
      tweet.location match {
        case None => 0
        case Some(loc) => {
          // We return the haversine distance between the `tweet` and the user, `me`
          val distance = dist(loc, me.location)
          val value = (10*math.min(1, 96700f / distance)) // roughly 1/10 of the vertical length of the uk in metres
          validate(value);
          return value;
        }
      }
      
    }
  }
  
  class Timeliness(me: User) extends Measure(me)
  // Measures relevance by how recent the tweet is
  {
    def measure(tweet: Tweet): Float = {
      var value = 0.0f; // Value to be returned
      val current = java.time.LocalDateTime.now()
      val tweetTime  = tweet.timestamp
      val ageInSecs = current.toEpochSecond(ZoneOffset.UTC) - tweetTime.toEpochSecond(ZoneOffset.UTC)
      val ageInDays = ageInSecs/60/60/24
      //value is a continuous function between 0 and 10. 
      //0mins old = 10
      //1 week old ~5
      //1 month old ~1
      value = 10/(1+ageInDays/7)
      validate(value);
      return value;
    }
  }
  
  class Hashtags(me: User) extends Measure(me)
  // Measures relevance by which hashtags the tweet has
  {
    def measure(tweet: Tweet): Float = {
      var value = 0.0f; // Value to be returned
      val hashtags = tweet.hashtags
      val hashtagsFollowing = me.hashtagsFollowing
      val usersize = hashtagsFollowing.size
      if (usersize == 0){return 0} // avoid div by 0
      var n=0

      // n is the number of matching hashtags
      for(following <- hashtagsFollowing)
      {
        for(tag <- hashtags)
        {
          if(following==tag){n = n+1}
        }
      }
      // 0<= n/usersize <= 1
      value = (n.toFloat/(usersize+1).toFloat)*10.0f

      validate(value);
      return value;
    }
  }
  
  class TweetPopularity(me: User) extends Measure(me)
  // Measures relevance by popularity of tweet
  {
    def measure(tweet: Tweet): Float = {
      var value = 0.0f; // Value to be returned
      
      val retweetFactor = 1;
      val favouriteFactor = 1;
      val cutoff = 5000;

      value = (tweet.retweets * retweetFactor + 
               tweet.favourites * favouriteFactor).toFloat / cutoff

      if (value >= 10) {value = 10}
      
      validate(value);
      return value;
    }
  }
  
  class UserPopularity(me: User) extends Measure(me)
  // Measures relevance by popularity of user
  {
    def measure(tweet: Tweet): Float = {
      var value = 0.0f; // Value to be returned
      
      value = tweet.user.followers_count;
      if (value > 1000000) value = 1000000;
      value./=(100000);
      
      validate(value);
      return value;
    }
  }
  
  class UserRelevance(me: User) extends Measure(me)
  // Measures relevance by proximity
  {
    def measure(tweet: Tweet): Float = {
      if (me.usersFollowing.contains(tweet.user.screen_name)) {10} else {0}
    }
  }
  
  /***** Sorting *****/
  
  // Defines a weight (given by the user) for each of the measures of relevance
  case class Weighting(proximity: Float, 
                       timeliness: Float, 
                       hashtags: Float, 
                       popularity: Float,
                       user: Float,
                       userRelevance: Float)
    

  
  case class Measures(proximity: Proximity, 
                      timeliness: Timeliness, 
                      hashtags: Hashtags, 
                      popularity: TweetPopularity,
                      user: UserPopularity,
                      userRelevance: UserRelevance)
  
  def score(tweet: Tweet, measures: Measures, weights: Weighting): Float =
  // Assigns a score to a given tweet using various measures of relevance
  {
    val scores = new Weighting(
      weights.proximity * measures.proximity.measure(tweet),
      weights.timeliness * measures.timeliness.measure(tweet),
      weights.hashtags * measures.hashtags.measure(tweet),
      weights.popularity * measures.popularity.measure(tweet),
      weights.user * measures.user.measure(tweet),
      weights.userRelevance * measures.userRelevance.measure(tweet)
    );
    tweet.scores = scores;
    val result = 
      scores.proximity + 
      scores.timeliness + 
      scores.hashtags + 
      scores.popularity + 
      scores.user +
      scores.userRelevance; 
    tweet.score = result;
    return result;
  }
  
  def sort(tweets: List[Tweet], user: User, weights:Weighting): List[Tweet] =
  // Takes a collection of tweets and sorts them by their score, for a particular user
  {
    val proximity = new Proximity(user);
    val timeliness = new Timeliness(user);
    val hashtags = new Hashtags(user);
    val popularity = new TweetPopularity(user);
    val userPopularity = new UserPopularity(user);
    val userRelevance = new UserRelevance(user);
    val measures = new Measures(proximity, timeliness, hashtags, popularity, userPopularity, userRelevance);
    
    return tweets.sortWith(score(_, measures, weights) > score(_, measures, weights));
  }
  
}
