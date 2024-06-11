import * as cart from "./shoppingCart.js";
import * as authentication from "./authentication.js";
import {removeExtraButtonsUser, wireupCartButton, wireupBundlesButton, wireupManagerButton} from "./header.js";


let tkn;

/**
 *
 * @param token
 */
export function setupUserPage(token)    {
    tkn = token;
    getBundles(token)
        .then((data) => {
            removeExtraButtonsUser();
            displayBundles(data);
            wireupCartButton(tkn);
            wireupBundlesButton(tkn);
            if(authentication.checkUserRole(token)){
                wireupManagerButton(tkn);
            }

        })
        .catch((error) => {
            console.log("Bro this is where we have the error");
            console.error(error);
            window.location.href = '/';
        });
}





export function displayBundles(data) {

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



