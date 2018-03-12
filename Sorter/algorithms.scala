import scala.List

object algorithms {

  /***** Tweets *****/
  
  class Tweet(id: int, user:User, hashtags: List[string], text: string, location: (float, float), retweets: int, favourites: int)
  // A Tweet object will represent a tweet and all its data
  {
    
  }
  
  
  /***** Users *****/
  
  class User(id: int, name: string, handle: string, location: string, followers: List[int]) 
  // A User object will represent a single user of the system and all their data
  {
    
  }
  
  
  /***** Measures of Relevance *****/
  
  abstract class Measure(me: User)
  // Each subclass of 'Measure' will define a measure of relevance for tweets
  // Relevance will be measured from the perspective of the user 'me'
  {
    def measure(tweet: Tweet): float // Takes a tweet and returns a value between 0 and 10
    
    protected def validate(value: float) = 
    // Checks that a value is between 0 and 10
    {
      assert (value >= 0);
      assert (value <= 10);
    }
  }
  
  class Location(me: User) extends Measure
  // Measures relevance by proximity
  {
    
  }
  
  class Timeliness(me: User) extends Measure
  // Measures relevance by how recent the tweet is
  {
    
  }
  
  class Hashtags(me: User) extends Measure
  // Measures relevance by which hashtags the tweet has
  {
    
  }
  
  class TweetPopularity(me: User) extends Measure
  // Measures relevance by popularity of tweet
  {
    
  }
  
  class UserPopularity(me: User) extends Measure
  // Measures relevance by popularity of user
  {
    
  }
  
  class UserRelationship(me: User) extends Measure
  // Measures relevance by proximity
  {
    
  }
  
  /***** Sorting *****/
  
  def score(tweet: Tweet, me: User): float
  // Assigns a score to a given tweet using various measures of relevance
  {
    
  }
  
  def sort(tweets: List[Tweet]): List[Tweet]
  // Takes a collection of tweets and sorts them by their score
  {
    
  }
  
}
