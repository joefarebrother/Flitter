
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

  function span(html){return $("<span>").html(html)}

  // display tweet
  var nextTweet = $("<div class='tweet'>")
  nextTweet.append(
    $("<p>") // profile pic could go here
      .append($("<span class='name'>").text(tweet.user.name))
      .append(span(" &nbsp;"))
      .append(
        $("<span class='handle'>").text("@" + tweet.user.screen_name))
      .append(span(" &nbsp;&nbsp;"))
      .append(
        $("<span class='relevance'>")
        .append("<i class='glyphicon glyphicon-stats'>")
        .append($("<span>").text(tweet.score)))
      .append(span(" &nbsp;"))
      .append(
        $("<span class='age'>")
        .append("<i class='glyphicon glyphicon-time'>")
        .append($("<span>").text(ageString))))

  var content = $("<div class='content'>")
  content.append($("<p class='text'>").text(tweet.text))
  //tweet images could go here

  nextTweet.append(content)

  nextTweet.append(
    $("<p>")
      .append("<i class='glyphicon glyphicon-heart'>")
      .append($("<span>").text(tweet.favourites))
      .append(span(" &nbsp; &nbsp; "))
      .append("<i class='glyphicon glyphicon-retweet'>")
      .append($("<span>").text(tweet.retweets)))

  $("#tweets").append(nextTweet)
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
