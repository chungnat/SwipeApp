const express = require('express');
const bodyParser = require('body-parser');
const router = express.Router();
const app = express();

app.use(bodyParser.json()); // support json encoded bodies
app.use(bodyParser.urlencoded({ extended: true })); // support encoded bodies
app.use('/', router);

var admin = require('firebase-admin');
var serviceAccount = require("H:\\SwipeApp\\TinderEatsServer\\tindereatsServiceAccountKey.json");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://tinder-eats.firebaseio.com"
});
var database = admin.database();

var matchListener = database.ref("matchDetails/");

matchListener.on("value", function(snapshot) {
  if(snapshot.exists)  {
      console.log("test");
      var data = snapshot.val();
      if(snapshot.val() === 'undefined') {  // no data yet
        console.log("null");
      }
      else {  // new data uploaded
        var uuid;
        var senderDisplayName;
        var senderUid;
        var invitedFriends = [];
        snapshot.forEach(function(childSnapshot) { // loop through all friends
          uuid = childSnapshot.val().uuid;
          senderDisplayName = childSnapshot.val().senderDisplayName;
          invitedFriends = childSnapshot.val().invitedFriends;
        });
        var uids = [];
        var registrationTokens = [];

        invitedFriends.forEach(element => {  // for each username, get their UID
            var getUid = database.ref("usernames/" + element);
            getUid.once('value').then(function(snapshot) { //add UID to list
              uids.push(snapshot.val());
              if(uids.length == invitedFriends.length) {  // if all UID added
                uids.forEach(element => {  // For each UID, get their registration token and update database
                  var getToken = database.ref("users/" + element);
                  getToken.once('value', function(snapshot) {
                      var userData = snapshot.val();
                      registrationTokens.push(userData.registrationToken);
                      if(registrationTokens.length == uids.length) { // if all registration tokens found
                        registrationTokens.forEach(element => {  // send a message to each device
                          var message = {
                            notification: {
                              title:'Tinder Eats',
                              body:'New match from ' + senderDisplayName
                            },
                            // data: {
                            //   friend: 'Nathan',
                            //   restaurant: 'Mcdonalds'
                            // },
                            token: element
                          };
                          admin.messaging().send(message)
                            .then((response) => {
                              // Response is a message ID string.
                              console.log('Successfully sent message:', response);
                            })
                            .catch((error) => {
                              console.log('Error sending message:', error);
                            });
                        });
                      }
                  });
                  //update database so recipients have a match
                  var matchRef = database.ref("matches/" + element + "/" + uuid);
                  matchRef.set("Received");
              });
              }
            });
        });
      }
  }
});

// Listen to the App Engine-specified port, or 8080 otherwise
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log(`Server listening at http://localhost:${PORT}`);
});


app.get('/', (req, res) => {
  res.send('Hello from App Engine!');
});

// Route that receives a POST request send a friend request
app.post('/sendFriendRequest',function (req, res) {
  var body = req.body;
  console.log(body.Username);
  console.log(body.Uid);
  var friendUid;
  var userRef = database.ref("usernames/" + body.Username);
  userRef.once('value').then(function(snapshot) {  // read database for friend's uid
    friendUid = snapshot.val();
    console.log(snapshot.val());
    console.log(friendUid);
    var senderRef = database.ref("friends/" + body.Uid + "/" + friendUid);
    senderRef.once('value').then(function(snapshot) {  // read database to see if friend already exists
      console.log(snapshot.val());
      if(snapshot.val() == null) {  // only set friends to pending if not already friends
        senderRef.set("Sent");
        var recieveRef = database.ref("friends/" + friendUid + "/" + body.Uid);
        recieveRef.set("Received");
        res.set('Content-Type', 'text/plain');
        res.send("Friend Request Sent");
      }
      else {
        res.send("Already Friends/Friend Request already sent");
      }
    });
  });
});

app.post('/getFriends', function(req, res) {
  var body = req.body;
  var uid = body.Uid;
  console.log("Function: get Friends")
  console.log(uid);
  const friendsList = [];
  var friendsRef = database.ref("friends/" + uid);
  friendsRef.once('value', function(snapshot) {
    snapshot.forEach(function(childSnapshot) { // loop through all friends
      var uid = childSnapshot.key;
      var status = childSnapshot.val();
      // Add friend's uid to list if status is "true"
      if(status == "True") {
        friendsList.push(uid);
      }
    });
    console.log(friendsList.length);
    const nameList = [];
    const usernameList = [];
    const iconList = [];
    // loop through list of uids to find name, username, and icon of each user
    // add each of these to a list
    friendsList.forEach(element => {
      var userRef = database.ref("users/" + element);
      userRef.once('value', function(snapshot) {
        var userData = snapshot.val();
        var displayName = userData.name;
        var username = userData.username;
        var icon = userData.icon;
        nameList.push(displayName);
        usernameList.push(username);
        iconList.push(icon);
        if(nameList.length == friendsList.length) {  // send response containing these lists
          res.json({names : nameList, usernames : usernameList, icons : iconList, size : friendsList.length});
        }
      })
    });
    // if no friends, will just send an empty list
    if(friendsList.length == 0) {
      res.json({names : nameList, usernames : usernameList, icons : iconList, size : 0});
    }
  });
});

app.post("/getPendingFriends", function(req, res) {
  var body = req.body;
  var uid = body.Uid;
  console.log("Function:Get pending Friends")
  console.log("Sender:" + uid);
  const friendsList = [];
  var friendsRef = database.ref("friends/" + uid);
  friendsRef.once('value', function(snapshot) {
    snapshot.forEach(function(childSnapshot) { // loop through all friends
      var uid = childSnapshot.key;
      var status = childSnapshot.val();
      // Add friend's uid to list if status is "true"
      if(status == "Received") {
        friendsList.push(uid);
      }
    });
    console.log("Result Length:" + friendsList.length);
    const nameList = [];
    const usernameList = [];
    const iconList = [];
    // loop through list of uids to find name, username, and icon of each user
    // add each of these to a list
    friendsList.forEach(element => {
      var userRef = database.ref("users/" + element);
      userRef.once('value', function(snapshot) {
        var userData = snapshot.val();
        var displayName = userData.name;
        var username = userData.username;
        var icon = userData.icon;
        nameList.push(displayName);
        usernameList.push(username);
        iconList.push(icon);
        if(nameList.length == friendsList.length) {  // send response containing these lists
          res.json({names : nameList, usernames : usernameList, icons : iconList, size : friendsList.length});
        }
      })
    });
    // if no friends, will just send an empty list
    if(friendsList.length == 0) {
      res.json({names : nameList, usernames : usernameList, icons : iconList, size : 0});
    }
  });
});

app.post("/acceptFriend", function(req, res) {
  var body = req.body;
  var uid = body.senderUid;
  var friendUsername = body.friendUsername;
  console.log("Function: accept Friend");
  console.log(uid);
  console.log(friendUsername);
  var ref = database.ref("usernames/" + friendUsername);
  ref.once('value').then(function(snapshot) {  // find the friend's UID
    var friendUid = snapshot.val();
    var userFriendsList = database.ref("friends/" + uid + "/" + friendUid);
    userFriendsList.set("True");
    var senderFriendsList = database.ref("friends/" + friendUid + "/" + uid);
    senderFriendsList.set("True");
    res.set('Content-Type', 'text/plain');
    res.send("Friend Request Accepted");
  });

});

app.post("/rejectFriend", function(req, res) {
  var body = req.body;
  var uid = body.senderUid;
  var friendUsername = body.friendUsername;
  console.log("Function: reject Friend");
  console.log(uid);
  console.log(friendUsername);
  var ref = database.ref("usernames/" + friendUsername);
  ref.once('value').then(function(snapshot) {  // find the friend's UID
    var friendUid = snapshot.val();
    var userFriendsList = database.ref("friends/" + uid + "/" + friendUid);
    userFriendsList.remove();
    var senderFriendsList = database.ref("friends/" + friendUid + "/" + uid);
    senderFriendsList.remove();
    res.set('Content-Type', 'text/plain');
    res.send("Friend Removed");
  });
});