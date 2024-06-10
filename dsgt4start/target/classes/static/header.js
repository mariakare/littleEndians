import {getBundles} from "./getContent.js";
import {setupManagerPage} from "./managerPage.js";
import {setToken, getCart} from "./shoppingCart.js";
import {displayBundles} from "./getContent.js";


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