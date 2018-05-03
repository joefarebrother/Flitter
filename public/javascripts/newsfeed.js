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
  var header = $("<span class='tweet_header'>")
  var content = $("<div class='content'>")
  var scores = $("<div class='scores'>")
  var footer = $("<p class='tweet_footer'>)")

  // Create tweet header
  header 
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
      .append(span("&nbsp;"))
      .append($("<span>").text(tweet.score))
      .click(()=>{
        $(".scores:visible").slideUp()
        scores.slideDown()
      }))
    .append(span(" &nbsp;"))
    .append(
      $("<span class='age'>")
      .append("<i class='glyphicon glyphicon-time'>")
      .append(span("&nbsp;"))
      .append($("<span>").text(ageString)))
  if (tweet.location) {
    header
      .append(span(" &nbsp;"))
      .append(
        $("<span class='location'>")
        .append("<i class='glyphicon glyphicon-map-marker'>")
        .append($("<span>").text(tweet.location.name)))

  }

  nextTweet.append(header)

  // Create histogram 
  var measures = {proximity: ["map-marker"], 
                  timeliness: ["time"], 
                  hashtags: ["tags"], 
                  userRelevance: ["user", "check"],
                  popularity: ["fire"],
                  user: ["user", "fire"]}
  $.map(measures, (icons, m) => {
    var wrap = $("<div class='bar_wrap'>")
    wrap.append(
      $("<div class='bar_container'>").append(
        $("<div class='bar'>")
        .addClass(m)
        .height(10*tweet.scores[m]+"%")))
    wrap.append(
      $("<i class='glyphicon'>")
      .addClass("glyphicon-"+icons[0]))
    if (icons.length > 1) {
      wrap.append(
        $("<sup>").append(
          $("<i class='glyphicon'>")
          .addClass("glyphicon-"+icons[1])))
    }

    scores.append(wrap)
  })

  nextTweet.append(scores)

  // Create tweet content
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
  footer
    .append("<i class='glyphicon glyphicon-heart'>")
    .append(span("&nbsp;"))
    .append($("<span>").text(tweet.favourites))
    .append(span(" &nbsp; &nbsp; "))
    .append("<i class='glyphicon glyphicon-retweet'>")
    .append(span("&nbsp;"))
    .append($("<span>").text(tweet.retweets))

  nextTweet.append(footer)

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
