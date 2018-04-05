$.map(users, u=>{addUser(u)})
$.map(hashtags, h=>{addHashtag(h)})

function addToList(name, errID, inputID, listID, remove){
  $(errID).text("");
  $(inputID).val("");
  var addition = $("<li>");       
  addition.append($("<span>").text(name));
  addition.append(
    $("<button class='clear'>")
    .append("<i class='glyphicon glyphicon-remove'>")
    .click(()=>{
      addition.remove();
      remove(name, addition);
    }));
  $(listID).append(addition);
}

function addUser(name){
  //console.log(name)
  addToList(name, "#userError","#addUserName", "#userList", unfollowUser);
}

$("#addUserButton").click(()=>{ 
  var name = $("#addUserName").val();
  if (userValid(name) && !followingUser(name) && userExists(name))
  {
    followUser(name);
  }
});

function followUser(name) {
  $.ajax({
    url: "api/followUser",
    data: {name:name.substring(1)},
    success: ()=>{
      addUser(name)
      users.push(name)
    },
    error: ()=>{
      $("#userError").text("An error occoured.")
    }
  })
}

function unfollowUser(name, elem) {
  $.ajax({
    url: "api/unfollowUser",
    data: {name:name.substring(1)},
    success: ()=>{
      elem.remove()
      users.splice(users.indexOf(name))
    },
    error: ()=>{
      $("#userError").text("An error occoured.")
    }
  })
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



function addHashtag(name){
  addToList(name, "#hashtagError", "#addHashtagName", "#hashtagList", unfollowHashtag)
}

$("#addHashtagButton").click(()=>{ 
  var name = $("#addHashtagName").val();
  if (hashtagValid(name) && !followingHashtag(name))
  {
    followHashtag(name);
  }
});

function followHashtag(name) {
  $.ajax({
    url: "api/followHashtag",
    data: {name:name.substring(1)},
    success: ()=>{
      addHashtag(name)
      hashtags.push(name)
    },
    error: ()=>{
      $("#hashtagError").text("An error occoured.")
    }
  })
}

function unfollowHashtag(name, elem) {
  $.ajax({
    url: "api/unfollowHashtag",
    data: {name:name.substring(1)},
    success: ()=>{
      elem.remove()
      hashtags.splice(hashtags.indexOf(name))
    },
    error: ()=>{
      $("#hashtagError").text("An error occoured.")
    }
  })
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