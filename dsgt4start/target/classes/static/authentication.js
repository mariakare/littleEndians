// Function to decode Firebase token
function decodeFirebaseToken(token) {
    const [, payloadBase64] = token.split('.');
    const payload = JSON.parse(atob(payloadBase64));
    return payload;
}

// Parse token and check if user has manager role
export function checkUserRole(token) {
    const payload = decodeFirebaseToken(token);
    if (payload && payload.roles && payload.roles.includes('manager')) {
        // User has manager role
        return true;
    } else {
        // User does not have manager role
        return false;
    }
}


export function showAuthenticated(username) {
    //document.getElementById("namediv").innerHTML = "Hello " + username;
    document.getElementById("logindiv").style.display = "none";
    document.getElementById("contentdiv").style.display = "flex";
    document.getElementById("divHeaderButtons").style.display = "flex";
}

export function showUnAuthenticated() {
    document.getElementById("namediv").innerHTML = "";
    document.getElementById("email").value = "";
    document.getElementById("password").value = "";
    document.getElementById("logindiv").style.display = "block";
    document.getElementById("contentdiv").style.display = "none";

    document.getElementById("divHeaderButtons").style.display = "none";
}