import scala.List
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._

object algorithms {

  /***** Tweets *****/
  
  class Tweet(id: Int, user:User, timestamp: java.time.LocalDateTime, hashtags: List[String], text: String, location: (Float, Float), retweets: Int, favourites: Int)
  // A Tweet object will represent a tweet and all its data
  {
    def getTime = timestamp
  }
  
  
  /***** Users *****/
  
  class User(id: Int, name: String, handle: String, location: String, followers: List[User]) 
  // A User object will represent a single user of the system and all their data
  {
    
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
  
  class Location(me: User) extends Measure(me)
  // Measures relevance by proximity
  {
    def measure(tweet: Tweet): Float = {
      var value = 0.0f; // Value to be returned
      
      //insert algoritm here
      
      validate(value);
      return value;
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
      
      //insert algoritm here
      
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
  
  class Weighting(location: Float, timeliness: Float, hashtags: Float, tweetPopularity: Float)
  // Defines a weight (given by the user) for each of the measures of relevance
  { }
  
  class Measures(location: Location, timeliness: Timeliness, hashtags: Hashtags, tweetPopularity: TweetPopularity)
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
    val location = new Location(user);
    val timeliness = new Timeliness(user);
    val hashtags = new Hashtags(user);
    val tweetPopularity = new TweetPopularity(user);
    val measures = new Measures(location, timeliness, hashtags, tweetPopularity);
    // ...
    return List(); // placeholder to make it compile
  }
  
}
