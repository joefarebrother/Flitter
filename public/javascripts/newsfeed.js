
function displayTweet(tweet)
// Takes a tweet object and adds the tweet to the bottom of the news feed
{
  // Round score to 2dp
  tweet.score = Math.round(tweet.score * 100)/100;
  
  // Calculate age from timestamp
  var timestamp = new Date(tweet.timestamp);
  var age = new Date() - timestamp;
  var ageString = '???';
  age /= 1000; // Convert to seconds
  if (age < 60) ageString = Math.round(age) + 's';
  else {
    age /= 60; // Convert to minutes
    if (age < 60) ageString = Math.round(age) + 'm';
    else {
      age /= 60; //Convert to hours
      if (age < 24) ageString = Math.round(age) + 'h';
      else {
        age /= 24; //Convert to days
        if (age < 7) ageString = Math.round(age) + 'd';
        else {
          age /= 7;
          ageString = Math.round(age) + 'w';
        }
      }
    }
  }
  
  // Display tweet
  var header = // user picture could go here
      "<p> <span class='name'>" + tweet.user.name + "</span> &nbsp;" +
      "<span class='handle'> @" + tweet.user.screen_name + "</span> &nbsp;&nbsp;" +
      "<span class='relevance'><i class='glyphicon glyphicon-stats'></i> " + tweet.score + "</span> &nbsp;" +
      "<span class='age'><i class='glyphicon glyphicon-time'></i> " + ageString + "</span></p>";
  var content = "<div class='content'>" +
      "<p class='text'>" + tweet.text + "</p>" +
      // tweet image could go here
      "</div>";
  var footer = "<p><i class='glyphicon glyphicon-heart'></i> " + tweet.favourites +
      " &nbsp; &nbsp; <i class='glyphicon glyphicon-retweet'></i> " + tweet.retweets + "</p>";
  
  var tweets = document.getElementById('tweets');
  var nextTweet = document.createElement('div');
  nextTweet.classList.add('tweet');
  nextTweet.innerHTML = header + content + footer;
  tweets.appendChild(nextTweet);
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

$.ajax({
  url: "api/getTweets",
  success: (data) => {displayTweets(data)},
  error: () => $("#nextTweet").addClass("error").text("An error occoured")
})
