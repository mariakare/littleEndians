import * as cart from "./shoppingCart.js";
import {setupManagerPage} from "./managerPage.js";
import * as authentication from "./authentication.js";

let tkn;

/**
 *
 * @param token
 */
export function setupUserPage(token)    {
    tkn = token;
    getBundles(token)
        .then((data) => {
            removeExtraButtons();
            displayBundles(data);
            cart.wireupCartButton(tkn);
            wireupBundlesButton(tkn);
            if(authentication.checkUserRole(token)){
                wireupManagerButton();
            }

        })
        .catch((error) => {
            console.error(error);
        });
}

function removeExtraButtons() {
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

// // Call the function to remove extra elements
// removeExtraElements();


function wireupManagerButton(){
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


function wireupBundlesButton(tkn){
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

/**
 *  Get all available bundles and display them as content on the page
 * @param token
 */
export function getBundles(token) {
    return fetch('/api/getBundles', {
        headers: { Authorization: 'Bearer ' + token}
    })
        .then((response) => {
            return response.text();
        })
        .then((data) => {
            return data;
        })
        .catch(function (error) {
            console.log(error);
        });


}

function displayBundles(data) {

    const bundles = JSON.parse(data).bundles;
    const contentDiv = document.getElementById('contentdiv');
    // Clear the contentdiv before adding new bundles
    contentDiv.innerHTML = '';
    // Loop through each bundle
    console.log(bundles);
    bundles.forEach(bundle => {
        // Create a div element for the bundle
        const bundleDiv = document.createElement('div');
        bundleDiv.classList.add('product');

        bundleDiv.innerHTML = `<h2 class="bundle-title">${bundle.name}</h2>`;
        const productBundleDiv = document.createElement('div');
        productBundleDiv.classList.add('product-bundle');

        // Loop through each product in the bundle
        bundle.products.forEach(product => {

            const productItemDiv = document.createElement('div');
            productItemDiv.classList.add('product-item');

            productItemDiv.innerHTML = `
        <img src="${product.imageLink}" alt="Product Image">
        <h3 class="product-title">${product.name}</h3>
        <p class="product-description">${product.description}</p>
      `;
            productBundleDiv.appendChild(productItemDiv);
        });

        bundleDiv.appendChild(productBundleDiv);
        // Add the bundle description and price
        bundleDiv.innerHTML += `
            <p class="bundle-description">${bundle.description}</p>
            <p class="bundle-price">$${bundle.price}</p>
        `;

        // Create the "Add to Cart" button
        const addToCartButton = document.createElement('a');
        addToCartButton.href = '#';
        addToCartButton.classList.add('add-to-cart');
        addToCartButton.textContent = 'Add to Cart';
        // Attach event listener to the "Add to Cart" button
        addToCartButton.addEventListener('click', function() {
            // Get the bundle ID from the bundle object (assuming each bundle has an ID)
            const bundleId = bundle.id; // Change this according to your bundle structure
            console.log(bundleId)
            // Call a function to add the bundle to the cart
            cart.addToCart(bundleId);
        });
        bundleDiv.appendChild(addToCartButton);

        contentDiv.appendChild(bundleDiv);
    });
}



