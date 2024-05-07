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
            displayBundles(data);
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

export function setupManagerPage(token){
    adaptHeaderManager();
    removeViewCartButton();
    getManager();
}
function getManager(token) {

    fetch('/api/getManagerBundles', {
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

function adaptHeaderManager(){
    // Create the tabs
    const tab1 = createTab("Active bundles", "/page1");
    const tab2 = createTab("Add new bunldes", "/page2");

    // Append tabs to the header menu
    const headerMenu = document.querySelector(".header-menu");
    headerMenu.insertBefore(tab2, headerMenu.lastElementChild); // Insert Page 2 tab before logout button
    headerMenu.insertBefore(tab1, tab2); // Insert Page 1 tab before Page 2 tab

    // Initially set the first tab as active
    setActiveTab(tab1);


}

// Function to create a tab element
function createTab(label, url) {
    const tab = document.createElement("div");
    tab.textContent = label;
    tab.classList.add("header-tab");
    tab.addEventListener("click", () => {
        setActiveTab(tab);
        // window.location.href = url;
        // ADD HERE WHAT SHOULD HAPPEN ON CLICK
    });
    return tab;
}

// Function to set active tab
function setActiveTab(tab) {
    const tabs = document.querySelectorAll(".header-tab");
    tabs.forEach(t => t.classList.remove("active"));
    tab.classList.add("active");
}



function removeViewCartButton() {
    const viewCartButton = document.getElementById("btnShoppingBasket");
    if (viewCartButton) {
        viewCartButton.parentNode.removeChild(viewCartButton);
    }
}