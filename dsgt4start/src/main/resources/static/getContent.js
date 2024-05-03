/**
 *  Get all available bundles and display them as content on the page
 * @param token
 */
export function getBundles(token) {

    fetch('/api/getBundles', {
        headers: { Authorization: 'Bearer ' + token}
    })
        .then((response) => {
            return response.text();
        })
        .then((data) => {

            console.log(data);
            displayBundles(data,token);
        })
        .catch(function (error) {
            console.log(error);
        });


}


function displayBundles(data,token) {

    const bundles = JSON.parse(data).bundles;
    const contentDiv = document.getElementById('contentdiv');
    // Clear the contentdiv before adding new bundles
    contentDiv.innerHTML = '';
    // Loop through each bundle
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
        <img src="${product.image}" alt="Product Image">
        <h3 class="product-title">${product.name}</h3>
        <p class="product-description">${product.description}</p>
      `;
            productBundleDiv.appendChild(productItemDiv);
        });

        bundleDiv.appendChild(productBundleDiv);
        bundleDiv.innerHTML += `<p class="bundle-description">${bundle.description}</p>`;

        // Create the "Add to Cart" button
        const addToCartButton = document.createElement('a');
        addToCartButton.href = '#';
        addToCartButton.classList.add('add-to-cart');
        addToCartButton.textContent = 'Add to Cart';
        // Attach event listener to the "Add to Cart" button
        addToCartButton.addEventListener('click', function() {
            // Get the bundle ID from the bundle object (assuming each bundle has an ID)
            const bundleId = bundle.name; // Change this according to your bundle structure
            console.log(bundleId)
            // Call a function to add the bundle to the cart
            addToCart(bundleId, token);
        });
        bundleDiv.appendChild(addToCartButton);

        contentDiv.appendChild(bundleDiv);
    });
}

function addToCart(bundleId, token) {
    // Send a fetch request to the backend
    fetch('/api/addToCart', {
        method: 'POST',
        headers: {
            Authorization: 'Bearer ' + token
        },
        body: bundleId
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(data => {
            // Handle success response (optional)
            console.log(data);
            alert('Bundle added to cart!');
        })
        .catch(error => {
            // Handle error response (optional)
            console.error(error);
            alert('Error adding bundle to cart: ' + error.message);
        });
}