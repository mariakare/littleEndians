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
            return data;

            //console.log(data);
            //displayBundles(data,token);
        })
        .catch(function (error) {
            console.log(error);
        });


}

export function getCart(token) {

    fetch('/api/getCart', {
        headers: { Authorization: 'Bearer ' + token}
    })
        .then((response) => {
            return response.json();
        })
        .then((data) => {
            console.log(data);
            displayShoppingCart(data,token);
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

function displayShoppingCart(data) {
    const contentDiv = document.getElementById('contentdiv');
    contentDiv.innerHTML = '';


    // Create elements to display shopping cart contents
    const heading = document.createElement('h2');
    heading.textContent = 'Shopping Cart';

    // Append heading to containerDiv
    contentDiv.appendChild(heading);

    data.forEach(item => {
        // Create a div for each item
        const itemDiv = document.createElement('div');
        itemDiv.classList.add('cart-item');

        // Display item name
        const itemName = document.createElement('span');
        itemName.textContent = item.name;
        itemDiv.appendChild(itemName);

        // Create a delete button for each item
        const deleteButton = document.createElement('button');
        deleteButton.textContent = 'Delete';
        deleteButton.addEventListener('click', function() {
            // Handle delete action
            deleteCartItem(item.id);
        });
        itemDiv.appendChild(deleteButton);

        contentDiv.appendChild(itemDiv);
    });
}

// Function to delete an item from the cart (to be implemented)
function deleteCartItem(itemId) {
    // Implement logic to delete the item from the cart
}





function displayManagerBundles(data) {
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

        // Add an "Edit Bundle" button
        const editButton = document.createElement('button');
        editButton.textContent = "Edit Bundle";
        editButton.classList.add('edit-button');
        editButton.style.backgroundColor = "#007bff"; // Set background color to blue
        editButton.style.color = "#fff"; // Set text color to white
        editButton.addEventListener('click', () => {
            // Call a function to handle editing of the bundle
            editBundle(bundle.id); // You need to implement this function
        });
        bundleDiv.appendChild(editButton);

        // Replace the "Add to Cart" button with a "Delete" button
        const deleteButton = document.createElement('button');
        deleteButton.textContent = "Delete Bundle";
        deleteButton.classList.add('delete-button');
        deleteButton.style.backgroundColor = "#dc3545"; // Set background color to red
        deleteButton.style.color = "#fff"; // Set text color to white

        deleteButton.addEventListener('click', () => {
            // Display confirmation window
            const isConfirmed = confirm("Are you sure you want to delete this bundle?");

            // If user confirms deletion
            if (isConfirmed) {
                // Call a function to handle deletion of the bundle
                deleteBundle(bundle.id); // You need to implement this function
            }
        });

        bundleDiv.appendChild(deleteButton);

        contentDiv.appendChild(bundleDiv);
    });
}


export function setupManagerPage(token){
    tkn = token;
    adaptHeaderManager();
    removeViewCartButton();
    getBundles(token)
        .then((data) => {
            displayManagerBundles(data);
            setupEditForm();
        })
        .catch((error) => {
            console.error(error);
        });

}
// function getManager(token) {
//
//     fetch('/api/getManagerBundles', {
//         headers: { Authorization: 'Bearer ' + token}
//     })
//         .then((response) => {
//             return response.text();
//         })
//         .then((data) => {
//             console.log(data);
//             displayBundles(data);
//         })
//         .catch(function (error) {
//             console.log(error);
//         });
//
//
// }

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
        if(label == "Active bundles"){
            //call function to display bundles
            getBundles(tkn)
                .then((data) => {
                    displayManagerBundles(data);
                })
                .catch((error) => {
                    console.error(error);
                });
        }
        else{
            //call function to display new bundles page

            //FOR NOW JUST CLEAR:
            const contentDiv = document.getElementById('contentdiv');
            // Clear the contentdiv before adding new bundles
            contentDiv.innerHTML = '';
        }
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



function deleteBundle(bundleId) {
    fetch(`/api/deleteBundle/${bundleId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${tkn}` // Include the token in the headers
        },
        // Add any other options if needed
    })
        .then(response => {
            if (response.ok) {
                // Bundle deleted successfully
                console.log("Bundle deleted successfully");
                displayManagerBundles(tkn);
            } else {
                // Error occurred while deleting the bundle
                console.error("Error deleting bundle:", response.statusText);
                // Optionally, you can display an error message to the user
            }
        })
        .catch(error => {
            console.error("Error deleting bundle:", error);
            // Optionally, you can display an error message to the user
        });
}


function setupEditForm(){
    // Get the modal
    const editBundleModal = document.getElementById("editBundleModal");

// Get the button that opens the modal
    const editButtons = document.querySelectorAll(".edit-button");

// Get the <span> element that closes the modal
    const closeBtn = document.querySelector(".close");

// When the user clicks the button, open the modal
    editButtons.forEach(button => {
        button.addEventListener("click", () => {
            // Get the current values of bundle title and description
            const bundleTitle = button.parentNode.querySelector(".bundle-title").textContent;
            const bundleDescription = button.parentNode.querySelector(".bundle-description").textContent;

            // Set the current values in the form
            document.getElementById("editBundleTitle").value = bundleTitle;
            document.getElementById("editBundleDescription").value = bundleDescription;

            // Show the modal
            editBundleModal.style.display = "block";
        });
    });

// When the user clicks on <span> (x), close the modal
    closeBtn.addEventListener("click", () => {
        editBundleModal.style.display = "none";
    });

// When the user clicks anywhere outside of the modal, close it
    window.addEventListener("click", (event) => {
        if (event.target == editBundleModal) {
            editBundleModal.style.display = "none";
        }
    });

// Handle form submission
    const editBundleForm = document.getElementById("editBundleForm");
    editBundleForm.addEventListener("submit", (event) => {
        event.preventDefault(); // Prevent the form from submitting normally

        // Get the edited values
        const editedBundleTitle = document.getElementById("editBundleTitle").value;
        const editedBundleDescription = document.getElementById("editBundleDescription").value;

        // Perform AJAX call to update the bundle with the new values
        // You need to implement this function
        updateBundle(bundleId, editedBundleTitle, editedBundleDescription);

        // Close the modal
        editBundleModal.style.display = "none";
    });

// Function to perform AJAX call to update the bundle
    function updateBundle(bundleId, editedBundleTitle, editedBundleDescription) {
        // Make your AJAX call here
    }

}