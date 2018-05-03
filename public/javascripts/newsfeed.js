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
  
  // Create tweet header
  nextTweet.append(
    $("<span>")
      .append($("<img>").attr("src", tweet.user.profile_image_url_https))
      .append(span(" &nbsp;&nbsp;"))
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

  // Create tweet content
  var content = $("<div class='content'>")
  content.append($("<p class='text'>").text(tweet.text))
  
  // Insert any pictures
  var i;
  var pics = tweet.image_urls;
  var n = pics.length;
  for (i = 0; i < n; i++)
  {
    content.append($("<img>").attr("src", pics[i]))
  }

  nextTweet.append(content)

  // Add tweet footer
  nextTweet.append(
    $("<p>")
      .append("<i class='glyphicon glyphicon-heart'>")
      .append($("<span>").text(tweet.favourites))
      .append(span(" &nbsp; &nbsp; "))
      .append("<i class='glyphicon glyphicon-retweet'>")
      .append($("<span>").text(tweet.retweets)))

  $("#tweets").append(nextTweet)

  // remove broken images 
  $("img").on("error", function(){$(this).remove()})
}

function displayTweets(tweets)
// Takes a sorted array of tweet objects and adds them all to the news feed
{
  var i;
  var n = tweets.length;
  for (i = 0; i < n; i++)
  {
    processTweet(tweets[i]);
    displayTweet(tweets[i]);
  }
}

function processTweet(tweet)
	//Processes the content of the tweet, removing links
	{
		tweet.text = tweet.text.replace(/https:\/\/t.co\/([a-z]|[A-Z]|\d)*\s/g, ' ');
		tweet.text = tweet.text.replace(/https:\/\/t.co\/([a-z]|[A-Z]|\d)*/g, '');
	}

$.ajax({
  url: "api/getTweets",
  success: (data) => {displayTweets(data)},
  error: () => $("#tweets").after($("<span class='error'>").text("An error occured"))
})
