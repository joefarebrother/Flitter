import scala.List
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._
import scala.math

object algorithms {
  // Location type is a tuple of floats, representing the Longitude and Latitude respectively
  type Location = (Float, Float)

  /***** Tweets *****/
  
  class Tweet(id: Int, 
              user:User, 
              timestamp: java.time.LocalDateTime, 
              hashtags: List[String], 
              text: String, 
              location: (Float, Float), 
              retweets: Int, favourites: Int)
  // A Tweet object will represent a tweet and all its data
  {
    def getTime = timestamp
    def getRetweets = retweets
    def getFavourites = favourites
    def getLocation = location
  }
  
  
  /***** Users *****/
  
  class User(id: Int, name: String, handle: String, location: Location, followers: List[User]) 
  // A User object will represent a single user of the system and all their data
  {
    def getLocation = location
  }
  
  
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
      // We use the haversine distance, as our location is given as longitude and latitudes
      val avgLongitude = (x._1 - y._1) / 2.0
      val avgLatitude = (x._2 - y._2) / 2.0
      // R is Earth's radius, in meters
      val R = 6371.0 * 1000.0
      
      // a = sin^2(avgLong) + cos(x._2) * cos(y._2) * sin^2(avgLat)
      val a = scala.math.pow(scala.math.sin(avgLongitude), 2.0) +
              scala.math.cos(x._1)*scala.math.cos(y._1)*scala.math.pow(scala.math.sin(avgLatitude), 2.0)
      // c = 2 * atan2(sqrt(a), sqrt(1-a))
      val c = 2.0 * scala.math.atan2(scala.math.sqrt(a), scala.math.sqrt(1.0-a))
      // d = R * c
      return (R * c).toFloat
    }
    
    def measure(tweet: Tweet): Float = {
      // We return the haversine distance between the `tweet` and the user, `me`
      val distance = dist(tweet.getLocation, me.getLocation)
      
      validate(distance);
      return distance;
    }
  }
  
  class Timeliness(me: User) extends Measure(me)
  // Measures relevance by how recent the tweet is
  {
    def measure(tweet: Tweet): Float = {
      var value = 0.0f; // Value to be returned
      val current = java.time.LocalDateTime.now()
      val tweetTime  = tweet.getTime
      val age = current.minusYears(tweetTime.getYear).minusDays(tweetTime.getDayOfYear).minusHours(tweetTime.getHour).minusMinutes(tweetTime.getMinute)
      val ageInMins = age.getYear * 525600 + age.getDayOfYear * 1440 + age.getHour * 60 + age.getMinute
      //if age > 1 month, score = 0
      //if age > 7 days, score = 3
      //if age > 1 day, score = 6
      //otherwise, score = ageInMins/1440 * 10
      if (ageInMins < 1440) {value = ((1440 - ageInMins.toFloat) / 1440).toFloat * 10}
        
      else if(ageInMins < 10080) value = 6
      
      else if (ageInMins < 43800) value = 3
      
      else value = 0
      
      validate(value);
      return value;
    }
  }
  
  class Hashtags(me: User) extends Measure(me)
  // Measures relevance by which hashtags the tweet has
  {
    def measure(tweet: Tweet): Float = {
      var value = 0.0f; // Value to be returned
      
      //insert algoritm here
      
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
      val cutoff = 100000;

      value = (tweet.getRetweets * retweetFactor + 
               tweet.getFavourites * favouriteFactor).toFloat / cutoff

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
      
      //insert algoritm here
      
      validate(value);
      return value;
    }
  }
  
  class UserRelationship(me: User) extends Measure(me)
  // Measures relevance by proximity
  {
    def measure(tweet: Tweet): Float = {
      var value = 0.0f; // Value to be returned
      
      //insert algoritm here
      
      validate(value);
      return value;
    }
  }
  
  /***** Sorting *****/
  
  class Weighting(proximity: Float, timeliness: Float, hashtags: Float, tweetPopularity: Float)
  // Defines a weight (given by the user) for each of the measures of relevance
  { }
  
  class Measures(proximity: Proximity, timeliness: Timeliness, hashtags: Hashtags, tweetPopularity: TweetPopularity)
  // Stores a Measure object for each of the measures of relevance
  { }
  
  def score(tweet: Tweet, measures: Measures, weights: Weighting): Float =
  // Assigns a score to a given tweet using various measures of relevance
  {
    /*return { // this doesn't compile
      weights.location * measures.location.measure(tweet) +
      weights.timeliness * measures.timeliness.measure(tweet) +
      weights.hashtags * measures.hashtags.measure(tweet) +
      weights.tweetPopularity * measures.tweetPopularity(tweet)
    }*/
    return 0 // placeholder
  }
  
  def sort(tweets: List[Tweet], user: User, weights:Weighting): List[Tweet] =
  // Takes a collection of tweets and sorts them by their score, for a particular user
  {
    val proximity = new Proximity(user);
    val timeliness = new Timeliness(user);
    val hashtags = new Hashtags(user);
    val tweetPopularity = new TweetPopularity(user);
    val measures = new Measures(proximity, timeliness, hashtags, tweetPopularity);
    // ...
    return List(); // placeholder to make it compile
  }
  
}