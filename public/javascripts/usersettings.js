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
      $("#userError").text("An error occured.")
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
      $("#userError").text("An error occured.")
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
      $("#hashtagError").text("An error occured.")
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
      $("#hashtagError").text("An error occured.")
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


/*** Location Picker ***/
var map;
var marker = false;
        
function initMap() {
 // Create map
    var centerOfMap = new google.maps.LatLng(51, 0);
 
    //Map options.
    var options = {
      center: centerOfMap,
      zoom: 7
    };
 
    // Create the map object
    map = new google.maps.Map(document.getElementById('map'), options);
 
    marker = new google.maps.Marker({
      position: {lat: Number($("#lat").val()), lng: Number($("#lng").val())},
      map: map,
      draggable: true
    })

    google.maps.event.addListener(marker, 'dragend', function(event){
      markerLocation();
    })

    // Listen for any clicks on the map
    google.maps.event.addListener(map, 'click', function(event) {                
      // Get the location that the user clicked
      var clickedLocation = event.latLng;
      // Marker has been added, so update its location
      marker.setPosition(clickedLocation);
      markerLocation();
    });
}
        
function markerLocation()
// Updates textboxes storing Latitude and Longitude
{
    //Get location
    var currentLocation = marker.getPosition();
    //Add lat and lng values to a field that we can save
    document.getElementById('lat').value = currentLocation.lat();
    document.getElementById('lng').value = currentLocation.lng();
}

$("#loc_update").click(()=>{
  $.ajax({
    url: "api/setLocation",
    data: {lat: $("#lat").val(), long: $("#lng").val},
    success: () => {
      $("#mapStatus").text("Successfully updated location").removeClass("error").fadeIn()
    },
    error: () => {
      $("#mapStatus").text("An error occured").addClass("error").fadeIn()
    }
  })
})
        
        
//Load the map when the page has finished loading
google.maps.event.addDomListener(window, 'load', initMap);
