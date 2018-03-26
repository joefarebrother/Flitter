var users = [];
var hashtags = [];

$("#addUserButton").click(()=>{ 
  var name = $("#addUserName").val();
  if (userValid(name) && !followingUser(name) && userExists(name))
  {
    $("#userError").text("");
    $("#addUserName").val("");
    var addition = $("<li>");       
    addition.append($("<span>").text(name));
    addition.append(
      $("<button class='clear'>")
      .append("<i class='glyphicon glyphicon-remove'>")
      .click(()=>{
        addition.remove();
        unfollowUser(name);
      }));
    $("#userList").append(addition);

    followUser(name);
  }
});

function followUser(name) {
  users.push(name);
}

function userValid(name) {
  var valid = true;
  if (name.substr(0,1) != '@'){valid = false;}
  if (!/^[A-Za-z0-9_]{1,15}$/.test(name.substr(1))){valid = false;}
  if (!valid){
    $('#userError').text('Invalid name. Names must start with @ and then contain only letters, numbers and underscores.');
  }
  return valid;
}

function followingUser(name) {
  var isFollowing = users.indexOf(name) > -1;
  if (isFollowing){
    $('#userError').text('You are already following this user.');
  }
  return isFollowing;
}

function userExists(name) {
  return true;
}

function unfollowUser(name) {
  users.splice(users.indexOf(name));
}

$("#addHashtagButton").click(()=>{ 
  var name = $("#addHashtagName").val();
  if (hashtagValid(name) && !followingHashtag(name))
  {
    $("#hashtagError").text("");
    $("#addHashtagName").val("");
    var addition = $("<li>");       
    addition.append($("<span>").text(name));
    addition.append(
      $("<button class='clear'>")
      .append("<i class='glyphicon glyphicon-remove'>")
      .click(()=>{
        addition.remove();
        unfollowHashtag(name);
      }));
    $("#hashtagList").append(addition);

    followHashtag(name);
  }
});

function followHashtag(name) {
}

function hashtagValid(name) {
  var valid = true;
  if (name.substr(0,1) != '#') {valid = false;}
  if (!/^[A-Za-z_][A-Za-z0-9_]{0,150}$/.test(name.substr(1))) {valid = false;}
  if (!valid) {
    $('#hashtagError').text('Invalid hashtag. Hashtags must start with # and then contain only letters, numbers and underscores. The # cannot be immediately followed by a number.');
  }
  return valid;
}

function followingHashtag(name) {
  var isFollowing = hashtags.indexOf(name) > -1;
  if (isFollowing){
    $('#hashtagError').text('You are already following this hashtag.');
  }
  return isFollowing;
}

function unfollowHashtag(name) {
  users.splice(users.indexOf(name));
}