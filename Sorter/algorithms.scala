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
    
  }
  
  class Timeliness(me: User) extends Measure(me)
  // Measures relevance by how recent the tweet is
  {
    def measure(tweet: Tweet): Float = {
      val current = java.time.LocalDateTime.now()
      val tweetTime  = tweet.getTime
      val age = current.minusYears(tweetTime.getYear).minusDays(tweetTime.getDayOfYear).minusHours(tweetTime.getHour).minusMinutes(tweetTime.getMinute)
      val ageInMins = age.getYear * 525600 + age.getDayOfYear * 1440 + age.getHour * 60 + age.getMinute
      //if age > 1 month, score = 0
      //if age > 7 days, score = 3
      //if age > 1 day, score = 6
      //otherwise, score = ageInMins/1440 * 10
      if (ageInMins < 1440) {return ((1440 - ageInMins.toFloat) / 1440).toFloat * 10}
        
      else if(ageInMins < 10080) return 6
      
      else if (ageInMins < 43800) return 3
      
      else return 0
      
      
      
    }
    // see https://www.hackingnote.com/en/scala/datetime/
    // and scroll down to section 'Java 8'
  }
  
  class Hashtags(me: User) extends Measure(me)
  // Measures relevance by which hashtags the tweet has
  {
    
  }
  
  class TweetPopularity(me: User) extends Measure(me)
  // Measures relevance by popularity of tweet
  {
    
  }
  
  class UserPopularity(me: User) extends Measure(me)
  // Measures relevance by popularity of user
  {
    
  }
  
  class UserRelationship(me: User) extends Measure(me)
  // Measures relevance by proximity
  {
    
  }
  
  /***** Sorting *****/
  
  class Weighting(location: Float, timeliness: Float, hashtags: Float, tweetPopularity: Float)
  //Defines a weight (given by the user) for each of the measures of relevance
  { }
  
  def score(tweet: Tweet, me: User, weights: Weighting): Float =
  // Assigns a score to a given tweet using various measures of relevance
  {
    
  }
  
  def sort(tweets: List[Tweet]): List[Tweet] =
  // Takes a collection of tweets and sorts them by their score
  {
    
  }
  
}
