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
      console.log("Hello " + auth.currentUser.email)

      //update GUI when user is authenticated
      showAuthenticated(auth.currentUser.email);

      console.log("Token: " + idTokenResult.token);

      //fetch data from server when authentication was successful. 
      var token = idTokenResult.token;
      fetchData(token);

    });

  });
}

function fetchData(token) {
  getHello(token);
  whoami(token);
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

function addContent(text) {
  document.getElementById("contentdiv").innerHTML += (text + "<br/>");
}

// calling /api/hello on the rest service to illustrate text based data retrieval
function getHello(token) {

  fetch('/api/getBundles', {
    headers: { Authorization: 'Bearer ' + token}
  })
    .then((response) => {
      return response.text();
    })
    .then((data) => {

      console.log(data);
      displayBundles(data);
    })
    .catch(function (error) {
      console.log(error);
    });


}
// calling /api/whoami on the rest service to illustrate JSON based data retrieval
function whoami(token) {

  fetch('/api/whoami', {
    headers: { Authorization: 'Bearer ' + token }
  })
    .then((response) => {
      return response.json();
    })
    .then((data) => {
      console.log(data.email + data.role);

    })
    .catch(function (error) {
      console.log(error);
    });

}


function displayBundles(data) {
// Parse the JSON data
  const bundles = JSON.parse(data).bundles;

  // Get the contentdiv container
  const contentDiv = document.getElementById('contentdiv');

  // Loop through each bundle
  bundles.forEach(bundle => {
    // Create a div element for the bundle
    const bundleDiv = document.createElement('div');
    bundleDiv.classList.add('product');

    // Set the bundle title
    bundleDiv.innerHTML = `<h2 class="bundle-title">${bundle.name}</h2>`;

    // Create a div element for the product bundle
    const productBundleDiv = document.createElement('div');
    productBundleDiv.classList.add('product-bundle');

    // Loop through each product in the bundle
    bundle.products.forEach(product => {
      // Create a div element for the product item
      const productItemDiv = document.createElement('div');
      productItemDiv.classList.add('product-item');

      // Set the product image, title, and description
      productItemDiv.innerHTML = `
        <img src="${product.image}" alt="Product Image">
        <h3 class="product-title">${product.name}</h3>
        <p class="product-description">${product.description}</p>
      `;

      // Append the product item to the product bundle
      productBundleDiv.appendChild(productItemDiv);
    });

    // Append the product bundle to the bundle div
    bundleDiv.appendChild(productBundleDiv);

    // Set the bundle description
    bundleDiv.innerHTML += `<p class="bundle-description">${bundle.description}</p>`;

    // Create a link to add the bundle to cart (you can modify this as needed)
    bundleDiv.innerHTML += `<a href="#" class="add-to-cart">Add to Cart</a>`;

    // Append the bundle div to the contentdiv container
    contentDiv.appendChild(bundleDiv);
  });
}