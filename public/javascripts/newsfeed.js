
function displayTweet(tweet)
// Takes a tweet object and adds the tweet to the bottom of the news feed
{
  var header = // user picture could go here
      "<p><span class='name'>" + tweet.user.name + "</span> &nbsp;" +
      "<span class='handle'>" + tweet.user.handle + "</span> &nbsp;&nbsp;" +
      "<span class='relevance'><i class='glyphicon glyphicon-stats'></i> " + tweet.relevance + "</span> &nbsp;" +
      "<span class='age'><i class='glyphicon glyphicon-time'></i> " + tweet.age + "</span></p>";
  var content = "<div class='content'>" +
      "<p class='text'>" + tweet.text + "</p>" +
      // tweet image could go here
      "</div>";
  var footer = "<p><i class='glyphicon glyphicon-heart'></i> " + tweet.favourites +
      " &nbsp; &nbsp; <i class='glyphicon glyphicon-retweet'></i> " + tweet.retweets + "</p>";
  var next = document.getElementById('nextTweet');
  next.outerHTML = "<div class='tweet'>" + header + content + footer +
      "</div><div id='nextTweet'></div>";
}

function displayTweets(tweets)
// Takes a sorted array of tweet objects and adds them all to the news feed
{
  var i;
  var n = tweets.length;
  for (i = 0; i < n; i++)
  {
    displayTweet(tweets[i]);
  }
}
