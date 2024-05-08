let tkn;

export function setupUserPage(token)    {
    tkn = token;
    getBundles(token)
        .then((data) => {
            displayBundles(data);
        })
        .catch((error) => {
            console.error(error);
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
        bundleDiv.innerHTML += `<a href="#" class="add-to-cart">Add to Cart</a>`;

        contentDiv.appendChild(bundleDiv);
    });
}