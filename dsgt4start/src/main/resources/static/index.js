import {
  initializeApp
} from "https://www.gstatic.com/firebasejs/9.9.4/firebase-app.js";
import {
  getAuth,
  connectAuthEmulator,
  onAuthStateChanged,
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
} from "https://www.gstatic.com/firebasejs/9.9.4/firebase-auth.js";

import {setupUserPage} from "./getContent.js";
import {setupManagerPage} from "./getContent.js";

// we setup the authentication, and then wire up some key events to event handlers
setupAuth();
wireGuiUpEvents();
wireUpAuthChange();

//setup authentication with local or cloud configuration. 
function setupAuth() {
  let firebaseConfig;
  if (location.hostname === "localhost") {
    firebaseConfig = {
      apiKey: "AIzaSyBoLKKR7OFL2ICE15Lc1-8czPtnbej0jWY",
      projectId: "demo-distributed-systems-kul",
    };
  } else {
    firebaseConfig = {
      // TODO: for level 2, paste your config here
    };
  }

  // signout any existing user. Removes any token still in the auth context
  const firebaseApp = initializeApp(firebaseConfig);
  const auth = getAuth(firebaseApp);
  try {
    auth.signOut();
  } catch (err) { }

  // connect to local emulator when running on localhost
  if (location.hostname === "localhost") {
    connectAuthEmulator(auth, "http://localhost:8082", { disableWarnings: true });
  }
}

function wireGuiUpEvents() {
  // Get references to the email and password inputs, and the sign in, sign out and sign up buttons
  var email = document.getElementById("email");
  var password = document.getElementById("password");
  var signInButton = document.getElementById("btnSignIn");
  var signUpButton = document.getElementById("btnSignUp");
  var logoutButton = document.getElementById("btnLogout");

  // Add event listeners to the sign in and sign up buttons
  signInButton.addEventListener("click", function () {
    // Sign in the user using Firebase's signInWithEmailAndPassword method

    signInWithEmailAndPassword(getAuth(), email.value, password.value)
      .then(function () {

        console.log("signedin");
      })
      .catch(function (error) {
        // Show an error message
        console.log("error signInWithEmailAndPassword:")
        console.log(error.message);
        alert(error.message);
      });
  });

  signUpButton.addEventListener("click", function () {
    // Sign up the user using Firebase's createUserWithEmailAndPassword method

    createUserWithEmailAndPassword(getAuth(), email.value, password.value)
      .then(function () {
        console.log("created");
      })
      .catch(function (error) {
        // Show an error message
        console.log("error createUserWithEmailAndPassword:");
        console.log(error.message);
        alert(error.message);
      });
  });

  logoutButton.addEventListener("click", function () {
    try {
      var auth = getAuth();
      auth.signOut();
    } catch (err) { }
  });

}

function wireUpAuthChange() {

  var auth = getAuth();
  onAuthStateChanged(auth, (user) => {
    console.log("onAuthStateChanged");
    if (user == null) {
      console.log("user is null");
      showUnAuthenticated();
      return;
    }
    if (auth == null) {
      console.log("auth is null");
      showUnAuthenticated();
      return;
    }
    if (auth.currentUser === undefined || auth.currentUser == null) {
      console.log("currentUser is undefined or null");
      showUnAuthenticated();
      return;
    }

    auth.currentUser.getIdTokenResult().then((idTokenResult) => {
      // console.log("Hello " + auth.currentUser.email)

      //update GUI when user is authenticated
      showAuthenticated(auth.currentUser.email);

      // console.log("Token: " + idTokenResult.token);

      //fetch data from server when authentication was successful. 
      var token = idTokenResult.token;
      const isManager = checkUserRole(token)
      console.log("Is user a manager? ", isManager);
      if(!isManager) setupUserPage(token);
      else setupManagerPage(token);
      //fetchData(token);

    });

  });
}


function showAuthenticated(username) {
  //document.getElementById("namediv").innerHTML = "Hello " + username;
  document.getElementById("logindiv").style.display = "none";
  document.getElementById("contentdiv").style.display = "flex";
  document.getElementById("divHeaderButtons").style.display = "flex";
}

function showUnAuthenticated() {
    document.getElementById("namediv").innerHTML = "";
    document.getElementById("email").value = "";
    document.getElementById("password").value = "";
    document.getElementById("logindiv").style.display = "block";
    document.getElementById("contentdiv").style.display = "none";

    document.getElementById("divHeaderButtons").style.display = "none";

}



// Function to decode Firebase token
function decodeFirebaseToken(token) {
  const [, payloadBase64] = token.split('.');
  const payload = JSON.parse(atob(payloadBase64));
  return payload;
}

// Parse token and check if user has manager role
function checkUserRole(token) {
  const payload = decodeFirebaseToken(token);
  if (payload && payload.roles && payload.roles.includes('manager')) {
    // User has manager role
    return true;
  } else {
    // User does not have manager role
    return false;
  }
}

