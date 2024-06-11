import {adaptHeaderManager} from "./header.js";
import {getBundles, setupUserPage} from "./getContent.js";
let tkn;
let currentBundle;

/**
 * Function used to setup the manager page. By default, opens the view bundles tab
 *  only works if token contains {"roles":"manager"}
 * @param token - auth token
 */
export function setupManagerPage(token){
    tkn = token;
    //adaptHeaderManager();
    //removeViewCartButton();
    getBundles(token)
        .then((data) => {
            displayManagerBundles(data);
            adaptHeaderManager();
            setupEditForm();
        })
        .catch((error) => {
            console.error(error);
        });

}







/// BELOW EVERYTHING FOR EDIT/DELETE BUNDLES PAGE


/**
 * Given json of all bundles, displays each bundle along with a edit and delete button
 * @param data json containing all bundles
 */
function displayManagerBundles(data) {
    const bundles = JSON.parse(data).bundles;
    const contentDiv = document.getElementById('contentdiv');
    // Clear the contentdiv before adding new bundles
    contentDiv.innerHTML = '';
    // Loop through each bundle
    bundles.forEach(bundle => {
        console.log(bundle);
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
        bundleDiv.appendChild(productBundleDiv);
        // Add the bundle description and price
        bundleDiv.innerHTML += `
            <p class="bundle-description">${bundle.description}</p>
            <p class="bundle-price">$${bundle.price}</p>
        `;

        // Add an "Edit Bundle" button
        const editButton = document.createElement('button');
        editButton.textContent = "Edit Bundle";
        editButton.classList.add('edit-button');
        editButton.style.backgroundColor = "#007bff"; // Set background color to blue
        editButton.style.color = "#fff"; // Set text color to white
        editButton.addEventListener('click', () => {
            // Call a function to handle editing of the bundle
            //editBundle(bundle.id); // You need to implement this function
            currentBundle = bundle.id;
            console.log("Displaying: " + currentBundle);
            console.log(bundle.id);
            //set global id var to current bundle id
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



// Function to set active tab
export function setActiveTab(tab) {
    const tabs = document.querySelectorAll(".header-tab");
    tabs.forEach(t => t.classList.remove("active"));
    tab.classList.add("active");
}



/**
 * Function to create tab for the page
 * @param label label for the tab
 * @returns {HTMLDivElement}
 */
export function createTab(label) {
    const tab = document.createElement("div");
    tab.textContent = label;
    tab.classList.add("header-tab", "dynamic-tab");
    switch (label){
        case "Active bundles":
            tab.addEventListener("click", () => {
                setActiveTab(tab);
                getBundles(tkn)
                    .then((data) => {
                        displayManagerBundles(data);
                        setupEditForm();
                    })
                    .catch((error) => {
                        console.error(error);
                    });
            });
            break;
        case "Add new bundles":
            tab.addEventListener("click", () => {
                setActiveTab(tab);
                getProducts(tkn)
                    .then((data) => {
                        displayProducts(data);
                    })
                    .catch((error) => {
                        console.error(error);
                    });
            });
            break;
        case "Customers":
            tab.addEventListener("click", () => {
                setActiveTab(tab);
                getCustomers(tkn)
                    .then((data) => {
                        displayCustomers(data);
                    })
                    .catch((error) => {
                        console.error(error);
                    });
            });
            break;
        case "Orders":
            tab.addEventListener("click", () => {
                setActiveTab(tab);
                getOrders(tkn)
                    .then((data) => {
                        console.log("I clicked get orders tab")
                        displayOrders(data);
                    })
                    .catch((error) => {
                        console.error(error);
                    });
            });
            break;
    }


    return tab;
}


function displayCustomers(jsonData){
    console.log(jsonData);
    const users = JSON.parse(jsonData);
    const contentDiv = document.getElementById('contentdiv');
    // Clear the contentDiv before adding new users
    contentDiv.innerHTML = '';
    // Create a list element
    const userList = document.createElement('ul');
    userList.id = 'user-list';

    users.forEach(user => {
        // Create a list item for the user
        const userItem = document.createElement('li');
        userItem.classList.add('user');

        userItem.innerHTML = `
            <div class="user-info">
                <p class="user-role">Role: ${user.role}</p>
                <p class="user-email">Email: ${user.email}</p>
            </div>
        `;

        // Append the user item to the userList
        userList.appendChild(userItem);
    });

    // Append the userList to the contentDiv
    contentDiv.appendChild(userList);
}

function displayOrders(jsonData) {
    // console.log("Received JSON data:", jsonData);
    // console.log("Ordered data:", jsonData["ordered"]);
    // console.log("Processing data:", jsonData["processing"]);
    // displaySec(jsonData["ordered"], "Ordered Items", true);
    // displaySec(jsonData["processing"], "Items Being Processed", false);
    // console.log("Received JSON data:", jsonData);
    // const orderedData = jsonData.hasOwnProperty("ordered") ? jsonData["ordered"] : [];
    // const processingData = jsonData.hasOwnProperty("processing") ? jsonData["processing"] : [];
    // displaySec(orderedData, "Ordered Items", true);
    // displaySec(processingData, "Items Being Processed", false);
    // Parse the JSON string into an object
    const responseObject = JSON.parse(jsonData);

// Extract the arrays based on their names
    const orderedArray = responseObject.ordered;
    // displaySec(processingArray, "Items Being Processed", true);
    displaySec(orderedArray, "Ordered Items", true);

}

function displaySec(data, head, clear) {
    const contentDiv = document.getElementById('contentdiv');
    if (clear) {
        contentDiv.innerHTML = '';
        console.log("Clearing contentDiv. Data to display:", data);
    }

    const cartDiv = document.createElement('div');
    cartDiv.classList.add('cart');

    // Create elements to display heading
    const heading = document.createElement('h2');
    heading.textContent = head;

    // Append heading to containerDiv
    cartDiv.appendChild(heading);

    data.forEach(order => {
        // Create a div for each order
        const orderDiv = document.createElement('div');
        orderDiv.classList.add('order');

        // Display order details
        const orderId = document.createElement('p');
        orderId.textContent = `Order ID: ${order.id}`;
        orderDiv.appendChild(orderId);

        const userId = document.createElement('p');
        userId.textContent = `User ID: ${order.userId}`;
        orderDiv.appendChild(userId);

        const bundleRef = document.createElement('p');
        bundleRef.textContent = `Bundle Ref: ${order.bundleRef}`;
        orderDiv.appendChild(bundleRef);

        // Append the orderDiv to the cartDiv
        cartDiv.appendChild(orderDiv);
    });

    // Append the cartDiv to the contentDiv
    contentDiv.appendChild(cartDiv);
}




/**
 * Basic setup for modal window used to edit bundles
 */
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
        console.log(currentBundle);
        updateBundle(currentBundle, editedBundleTitle, editedBundleDescription);


        // Close the modal
        editBundleModal.style.display = "none";

    });


}



/// EVERYTHING FOR NEW BUNDLE PAGE BELOW:


function displayProducts(data){
    var html = '';
    data = JSON.parse(data);
    // Check if data is valid
    if (!data || !data.suppliers || !Array.isArray(data.suppliers)) {
        console.error('Invalid data format.');
        return;
    }

    // Parse JSON data
    var suppliers = data.suppliers;

    // Open container div for columns
    html += '<div class="supplier-columns">';

    // Loop through suppliers
    for (var i = 0; i < suppliers.length; i++) {
        var supplier = suppliers[i];
        html += '<div class="supplier-column">';
        html += '<h2>' + supplier.name + '</h2>'; // Title the column with supplier name

        // Check if products exist and is an array
        if (supplier.products && Array.isArray(supplier.products)) {
            // Loop through products of the supplier
            var products = supplier.products;
            for (var j = 0; j < products.length; j++) {
                var product = products[j];
                html += '<div class="new-product">';
                html += '<input type="radio" name="supplier_' + i + '" value="' + supplier.name + "@" +  product.id + '">'; // Radio button for product
                html += '<img src="' + product.imageLink + '" alt="' + product.name + '">'; // Product image
                html += '<div class="new-details">';
                html += '<h3>' + product.name + '</h3>'; // Product name
                html += '<p>Price: $' + product.price.toFixed(2) + '</p>'; // Product price
                html += '<p>' + product.description + '</p>'; // Product description
                html += '</div>'; // End new-details
                html += '</div>'; // End new-product
            }
        } else {
            console.error('Products data missing or invalid for supplier:', supplier.name);
        }

        html += '</div>'; // End supplier-column
    }

    // Close container div for columns
    html += '</div>'; // End supplier-columns

    // Add extra fields for bundle title and description
    html += '<div class="bundle-fields">';
    html += '<label for="bundleTitle">Bundle Title:</label>';
    html += '<input type="text" id="bundleTitle" name="bundleTitle">';
    html += '<label for="bundleDescription">Bundle Description:</label>';
    html += '<textarea id="bundleDescription" name="bundleDescription"></textarea>';
    html += '</div>'; // End bundle-fields

    // Add Complete button
    html += '<button id="completeButton">Add New Bundle To Stock</button>';

    const contentDiv = document.getElementById('contentdiv');
    contentDiv.innerHTML = html;


    const completeButton = document.getElementById('completeButton');
    if (completeButton) {
        completeButton.addEventListener('click', checkValidBundle);
    }
}


function checkValidBundle() {
    // Your code to check the validity of the bundle goes here
    console.log('Checking the validity of the bundle...');

    let valid = true;
    let selectedProductIds = [];
    //ADD CODE HERE TO CHECK VALID BUNDLE
    let j=0;
    for (let i = 0; i < 4; i++) {
        const radioGroupName = 'supplier_' + i;
        const radioButtons = document.querySelectorAll('input[type="radio"][name="' + radioGroupName + '"]');
        // I now have the radio buttons belonging to a specific supplied
        let checked = false;
        radioButtons.forEach(function(radioButton) {
            if (radioButton.checked) {
                checked = true; // At least one radio button is checked
                j++;
                selectedProductIds.push(radioButton.value)
            }
        });

        // if (!checked) {
        //     valid = false;
        //     break;
        // }
    }

    if(j!=3){
        valid=false;
    }

    const bundleTitle = document.getElementById('bundleTitle').value;
    const bundleDescription = document.getElementById('bundleDescription').value;

    if(bundleTitle === "" || bundleDescription === ""){valid = false}


    if(valid){
        // Display a confirmation dialog
        var confirmed = window.confirm('Are you sure you want to add this bundle?');
        if (confirmed) {
            addBundle(selectedProductIds, bundleTitle, bundleDescription);
        }


    }
    else{
        window.alert('The bundle is not valid. Please ensure you have one item per supplier!');
    }
}






/// BELOW WE HAVE ALL CALLS TO THE SERVER



function addBundle(selectedProductIds, bundleTitle, bundleDescription){
    // Construct the request body
    const body = new URLSearchParams();
    body.append('productIds', JSON.stringify(selectedProductIds));
    body.append('bundleTitle', bundleTitle); // Add bundle title to request body
    body.append('bundleDescription', bundleDescription); // Add bundle description to request body

    // Make the fetch request
    fetch('/api/addBundle', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + tkn,
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: body
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then((data) => {
            // Handle the response data if needed
            console.log('Bundle added successfully:', data);
            window.alert('The bundle has been added successfully');
            return data;
        })
        .catch((error) => {
            console.error('Error adding bundle:', error);
            throw error; // Re-throw the error to propagate it further
        });
}


/**
 * API call to delete a given bundle
 * @param bundleId
 */
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
                getBundles(tkn)
                    .then((bundledata) => {
                        displayManagerBundles(bundledata);
                        setupEditForm();
                        // displayManagerBundles(data);
                        // // Reattach event listeners after displaying bundles
                        // attachEventListeners();
                    })
                    .catch((error) => {
                        console.error(error);
                    });
                //createTab("Active bundles");
                //displayManagerBundles(tkn);
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

function updateBundle(bundleId, bundleTitle, bundleDescription){
    // Construct the request body
    const body = new URLSearchParams();
    body.append('bundleId', bundleId); // Add bundle ID to request body
    body.append('bundleTitle', bundleTitle); // Add updated bundle title to request body
    body.append('bundleDescription', bundleDescription); // Add updated bundle description to request body

    // Make the fetch request
    fetch('/api/updateBundle', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + tkn,
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: body
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then((data) => {
            // Handle the response data if needed
            console.log('Bundle updated successfully:', data);
            getBundles(tkn)
                .then((bundledata) => {
                    displayManagerBundles(bundledata);
                    setupEditForm();
                    // displayManagerBundles(data);
                    // // Reattach event listeners after displaying bundles
                    // attachEventListeners();
                })
                .catch((error) => {
                    console.error(error);
                });
            //createTab("Active bundles");
            //displayManagerBundles(tkn);

            return data;
        })
        .catch((error) => {
            console.error('Error updating bundle:', error);
            throw error; // Re-throw the error to propagate it further
        });
}

function getProducts() {
    return fetch('/api/getProducts', {
        headers: { Authorization: 'Bearer ' + tkn}
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

function getCustomers() {
    return fetch('/api/getAllCustomers', {
        headers: { Authorization: 'Bearer ' + tkn}
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

function getOrders() {
    return fetch('/api/getAllOrders', {
        headers: { Authorization: 'Bearer ' + tkn}
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








/// NO LONGER NEEDED BUT KEEPING JUST IN CASE:

// /**
//  * Attaches event listeners to edit button and the close button of tttttthe modal window
//  */
// function attachEventListeners() {
//     // Get the modal
//     const editBundleModal = document.getElementById("editBundleModal");
//
//     // Get the button that opens the modal
//     const editButtons = document.querySelectorAll(".edit-button");
//
//     // Get the <span> element that closes the modal
//     const closeBtn = document.querySelector(".close");
//
//     // When the user clicks the button, open the modal
//     editButtons.forEach(button => {
//         button.addEventListener("click", () => {
//             // Get the current values of bundle title and description
//             const bundleTitle = button.parentNode.querySelector(".bundle-title").textContent;
//             const bundleDescription = button.parentNode.querySelector(".bundle-description").textContent;
//
//             // Set the current values in the form
//             document.getElementById("editBundleTitle").value = bundleTitle;
//             document.getElementById("editBundleDescription").value = bundleDescription;
//
//             // Show the modal
//             editBundleModal.style.display = "block";
//         });
//     });
//
//     // When the user clicks on <span> (x), close the modal
//     closeBtn.addEventListener("click", () => {
//         editBundleModal.style.display = "none";
//     });
//
//     // When the user clicks anywhere outside of the modal, close it
//     window.addEventListener("click", (event) => {
//         if (event.target == editBundleModal) {
//             editBundleModal.style.display = "none";
//         }
//     });
//
//     // Handle form submission
//     const editBundleForm = document.getElementById("editBundleForm");
//     editBundleForm.addEventListener("submit", (event) => {
//         event.preventDefault(); // Prevent the form from submitting normally
//
//         // Get the edited values
//         const editedBundleTitle = document.getElementById("editBundleTitle").value;
//         const editedBundleDescription = document.getElementById("editBundleDescription").value;
//
//         // Perform AJAX call to update the bundle with the new values
//         // You need to implement this function
//         updateBundle(currentBundle, editedBundleTitle, editedBundleDescription);
//
//         // Close the modal
//         editBundleModal.style.display = "none";
//     });
// }