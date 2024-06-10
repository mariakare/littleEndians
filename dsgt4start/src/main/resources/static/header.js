import {getBundles, setupUserPage} from "./getContent.js";
import {setupManagerPage, createTab} from "./managerPage.js";
import {setToken, getCart} from "./shoppingCart.js";
import {displayBundles} from "./getContent.js";

let tkn;

export function setupToken(token){tkn = token}

/// EVERYTHING FOR USER PAGE
export function removeExtraButtonsUser() {
    const headerButtonsContainer = document.getElementById("divHeaderButtons");
    const headerMenu = document.querySelector(".header-menu");

    const originalButtons = ["btnShoppingBasket", "btnLogout"];
    const buttons = headerButtonsContainer.getElementsByTagName("button");
    const buttonsArray = Array.from(buttons);

    buttonsArray.forEach(button => {
        if (!originalButtons.includes(button.id)) {
            headerButtonsContainer.removeChild(button);
        }
    });

    // Remove dynamically added tabs
    const dynamicTabs = headerMenu.querySelectorAll('.dynamic-tab');
    dynamicTabs.forEach(tab => {
        headerMenu.removeChild(tab);
    });

    // Ensure the cart button is visible again
    const viewCartButton = document.getElementById("btnShoppingBasket");
    if (viewCartButton) {
        viewCartButton.style.display = "block";
    }
}

export function wireupCartButton(token){
    let tkn = token;
    setToken(tkn);
    const viewCartButton = document.getElementById("btnShoppingBasket");
    if (viewCartButton.style.display === "none") {
        viewCartButton.style.display = "block";
    }
    viewCartButton.addEventListener('click', function() {
        getCart(tkn);
    });

}


export function wireupBundlesButton(tkn){
    const headerButtonsContainer = document.getElementById("divHeaderButtons"); // Assuming the header element has an ID of "header"

    // Create a new button element
    const bundlesButton = document.createElement("button");
    bundlesButton.id = "btnBundles"; // Set an ID for the new button
    bundlesButton.textContent = "Explore Bundles"; // Set the button text
    bundlesButton.style.display = "block"; // Ensure the button is visible

    // Append the button to the header
    const firstButton = headerButtonsContainer.firstChild;
    headerButtonsContainer.insertBefore(bundlesButton, firstButton);

    // Add an event listener to call the display() function when clicked
    bundlesButton.addEventListener('click', function() {
        getBundles(tkn)
            .then((data) => {
                // wireupBundlesButton(tkn);
                displayBundles(data);
                // cart.wireupCartButton(tkn);

            })
            .catch((error) => {
                console.error(error);
            });// Assuming display function might need the token as a parameter
    });
}


export function wireupManagerButton(tkn){
    const headerButtonsContainer = document.getElementById("divHeaderButtons"); // Assuming the header element has an ID of "header"

    // Create a new button element
    const managerButton = document.createElement("button");
    managerButton.id = "btnManager"; // Set an ID for the new button
    managerButton.textContent = "ManagerPage"; // Set the button text
    managerButton.style.display = "block"; // Ensure the button is visible

    // Append the button to the header
    const firstButton = headerButtonsContainer.firstChild;
    headerButtonsContainer.insertBefore(managerButton, firstButton);

    // Add an event listener to call the display() function when clicked
    managerButton.addEventListener('click', function() {
        setupManagerPage(tkn);
    });
}



/// EVERYTHING FOR MANAGER PAGE


/**
 * Adapts the header for the manager page - adds 2 tabs. 1 for viewing all bundles and editing them
 * and one for adding new bundles
 */
export function adaptHeaderManager() {
    // remove buttons
    removeExtraButtonsManager();
    addExitManagerButton();

    // Create the tabs
    const tab1 = createTab("Active bundles");
    const tab2 = createTab("Add new bundles");
    const tab3 = createTab("Customers");
    const tab4 = createTab("Orders");

    // Append tabs to the header menu
    const headerMenu = document.querySelector(".header-menu");
    headerMenu.insertBefore(tab4, headerMenu.lastElementChild); // Insert Page 2 tab before logout button
    headerMenu.insertBefore(tab3, tab4); // Insert Page 1 tab before Page 2 tab
    headerMenu.insertBefore(tab2, tab3);
    headerMenu.insertBefore(tab1, tab2);


    //remove cart button
    const viewCartButton = document.getElementById("btnShoppingBasket");
    if (viewCartButton) {
        viewCartButton.style.display = "none";
    }

    // Initially set the first tab as active
    setActiveTab(tab1);
}

function removeExtraButtonsManager() {
    const headerButtonsContainer = document.getElementById("divHeaderButtons");
    const originalButtons = ["btnShoppingBasket", "btnLogout"];

    // Get all buttons inside the container
    const buttons = headerButtonsContainer.getElementsByTagName("button");

    // Convert HTMLCollection to an array to safely iterate and remove elements
    const buttonsArray = Array.from(buttons);

    buttonsArray.forEach(button => {
        if (!originalButtons.includes(button.id)) {
            headerButtonsContainer.removeChild(button);
        }
    });
}

function addExitManagerButton(){
    const headerButtonsContainer = document.getElementById("divHeaderButtons"); // Assuming the header element has an ID of "header"

    // Create a new button element
    const returnButton = document.createElement("button");
    returnButton.id = "btnReturn"; // Set an ID for the new button
    returnButton.textContent = "User Page"; // Set the button text
    returnButton.style.display = "block"; // Ensure the button is visible

    // Append the button to the header
    const firstButton = headerButtonsContainer.firstChild;
    headerButtonsContainer.insertBefore(returnButton, firstButton);

    // Add an event listener to call the display() function when clicked
    returnButton.addEventListener('click', function() {
        setupUserPage(tkn);
    });
}





