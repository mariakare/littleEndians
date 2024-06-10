let tkn;

function getCart() {

    fetch('/api/getCart', {
        headers: { Authorization: 'Bearer ' + tkn}
    })
        .then((response) => {
            return response.json();
        })
        .then((data) => {
            console.log(data);
            // displayShoppingCart(data);
            displayCartPage(data);
        })
        .catch(function (error) {
            console.log(error);
        });


}

export function addToCart(bundleId) {
    // Send a fetch request to the backend
    fetch('/api/addToCart', {
        method: 'POST',
        headers: {
            Authorization: 'Bearer ' + tkn
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
function displayCartPage(data){
    // data = {
    //     "items": [
    //         {
    //             "category": "cart",
    //             "bundles": [
    //                 { "id": "N1Sm1vAxv6zCPBuzlr6V", "name": "broooooooo", "bundleId": "vKnOKRwG09xeSbEZzvRc" },
    //                 { "id": "T56uTvFi9LUODMEhchBI", "name": "broooooooo", "bundleId": "vKnOKRwG09xeSbEZzvRc" },
    //                 { "id": "tmYLPoteOw3q1ZOz3Lzr", "name": "broooooooo", "bundleId": "vKnOKRwG09xeSbEZzvRc" }
    //             ]
    //         },
    //         {
    //             "category": "shipping",
    //             "bundles": [
    //                 { "id": "N1Sm1vAxv6zCPBuzlr6V", "name": "broooooooo", "bundleId": "vKnOKRwG09xeSbEZzvRc" },
    //                 { "id": "T56uTvFi9LUODMEhchBI", "name": "broooooooo", "bundleId": "vKnOKRwG09xeSbEZzvRc" },
    //                 { "id": "tmYLPoteOw3q1ZOz3Lzr", "name": "broooooooo", "bundleId": "vKnOKRwG09xeSbEZzvRc" }
    //             ]
    //         },
    //         {
    //             "category": "past",
    //             "bundles": [
    //                 { "id": "N1Sm1vAxv6zCPBuzlr6V", "name": "broooooooo", "bundleId": "vKnOKRwG09xeSbEZzvRc" },
    //                 { "id": "T56uTvFi9LUODMEhchBI", "name": "broooooooo", "bundleId": "vKnOKRwG09xeSbEZzvRc" },
    //                 { "id": "tmYLPoteOw3q1ZOz3Lzr", "name": "broooooooo", "bundleId": "vKnOKRwG09xeSbEZzvRc" }
    //             ]
    //         }
    //     ]
    // };


    displaySec(findCategory("cart", data), "Cart", true, true);
    displaySec(findCategory("shipping", data), "Orders on their way to you", false, false);
    displaySec(findCategory("past", data), "Past orders", false, false);
}

function displaySec(data, head, clear, isCart){
    const contentDiv = document.getElementById('contentdiv');
    if (clear){
        contentDiv.innerHTML = '';
    }


    const cartDiv = document.createElement('div');
    cartDiv.classList.add('cart');

    // Create elements to display shopping cart contents
    const heading = document.createElement('h2');
    heading.textContent = head;

    // Append heading to containerDiv
    cartDiv.appendChild(heading);

    data.forEach(item => {
        // Create a div for each item
        const itemDiv = document.createElement('div');
        itemDiv.classList.add('cart-item');

        // Display item name
        const itemName = document.createElement('span');
        itemName.textContent = item.name;
        itemDiv.appendChild(itemName);


        if(isCart){
            // Create a buy button for each item
            const buyButton = document.createElement('button');
            buyButton.textContent = 'Buy';
            buyButton.classList.add('buy-button');
            buyButton.addEventListener('click', function() {
                // Handle delete action
                console.log(item);
                buyBundle(item.id);
            });
            itemDiv.appendChild(buyButton);

            // Create a delete button for each item
            const deleteButton = document.createElement('button');
            deleteButton.textContent = 'Delete';
            deleteButton.classList.add('delete-button');
            deleteButton.addEventListener('click', function() {
                // Handle delete action
                console.log(item);
                deleteCartBundle(item.id);
            });
            itemDiv.appendChild(deleteButton);

        }

        cartDiv.appendChild(itemDiv);
    });

    if(isCart){
        // Create the buy button
        const buyButton = document.createElement('button');
        buyButton.textContent = 'Buy';
        buyButton.id = 'buy-cart';
        buyButton.addEventListener('click', function() {
            // Handle buy action
            buyAll();
            // You can add your buy functionality here
        });

        // Append the buy button to the cartDiv
        cartDiv.appendChild(buyButton);
    }

    // Append the cartDiv to the contentDiv
    contentDiv.appendChild(cartDiv);
}
function displaySec(data, head, clear, isCart){
    const contentDiv = document.getElementById('contentdiv');
    if (clear){
        contentDiv.innerHTML = '';
    }


    const cartDiv = document.createElement('div');
    cartDiv.classList.add('cart');

    // Create elements to display shopping cart contents
    const heading = document.createElement('h2');
    heading.textContent = head;

    // Append heading to containerDiv
    cartDiv.appendChild(heading);

    data.forEach(item => {
        // Create a div for each item
        const itemDiv = document.createElement('div');
        itemDiv.classList.add('cart-item');

        // Display item name
        const itemName = document.createElement('span');
        itemName.textContent = item.name;
        itemDiv.appendChild(itemName);


        if(isCart){
            // Create a buy button for each item
            const buyButton = document.createElement('button');
            buyButton.textContent = 'Buy';
            buyButton.classList.add('buy-button');
            buyButton.addEventListener('click', function() {
                // Handle delete action
                console.log(item);
                buyBundle(item.id);
            });
            itemDiv.appendChild(buyButton);

            // Create a delete button for each item
            const deleteButton = document.createElement('button');
            deleteButton.textContent = 'Delete';
            deleteButton.classList.add('delete-button');
            deleteButton.addEventListener('click', function() {
                // Handle delete action
                console.log(item);
                deleteCartBundle(item.id);
            });
            itemDiv.appendChild(deleteButton);

        }

        cartDiv.appendChild(itemDiv);
    });

    if(isCart){
        // Create the buy button
        const buyButton = document.createElement('button');
        buyButton.textContent = 'Buy';
        buyButton.id = 'buy-cart';
        buyButton.addEventListener('click', function() {
            // Handle buy action
            buyAll();
            // You can add your buy functionality here
        });

        // Append the buy button to the cartDiv
        cartDiv.appendChild(buyButton);
    }

    // Append the cartDiv to the contentDiv
    contentDiv.appendChild(cartDiv);
}
function findCategory(categoryName, jsonData) {
    const item = jsonData.items.find(item => item.category.toLowerCase() === categoryName.toLowerCase());
    if (item) {
        return item.bundles;
    } else {
        console.log(`Category ${categoryName} not found.`);
    }
}




function findCategory(categoryName, jsonData) {
    const item = jsonData.items.find(item => item.category.toLowerCase() === categoryName.toLowerCase());
    if (item) {
        return item.bundles;
    } else {
        console.log(`Category ${categoryName} not found.`);
    }
}


function buyAll(){
    const buyButtons = document.querySelectorAll('.cart-item .buy-button');
    const promises = [];

    buyButtons.forEach(button => {
        const bundleId = button.closest('.cart-item').dataset.bundleId;
        promises.push(buyBundle(bundleId));
    });

    Promise.allSettled(promises)
        .then(results => {
            const failedBuys = results.filter(result => result.status === 'rejected');
            if (failedBuys.length > 0) {
                alert('Some items could not be bought. Please try again.');
            } else {
                alert('All items bought successfully!');
            }
        })
        .catch(error => {
            console.error('Error during the buy process:', error);
            alert('Some items could not be bought. Please try again.');
        });
}




export function wireupCartButton(token){
    tkn = token;
    const viewCartButton = document.getElementById("btnShoppingBasket");
    if (viewCartButton.style.display === "none") {
        viewCartButton.style.display = "block";
    }
    viewCartButton.addEventListener('click', function() {
        getCart(tkn);
    });

}


export function addToCart(bundleId) {
    // Send a fetch request to the backend
    fetch('/api/addToCart', {
        method: 'POST',
        headers: {
            Authorization: 'Bearer ' + tkn
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

function buyBundle(bundleId) {
    return fetch('/api/buyBundle', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: 'Bearer ' + tkn
        },
        body: JSON.stringify({ bundleId: bundleId })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to buy item');
            }
            console.log('Item bought successfully');
            getCart(); // Refresh the cart after buying an item
            window.buySuccess = true;
            return true;
        })
        .catch(error => {
            console.error('Error buying item:', error);
            getCart(); // Refresh the cart even if there's an error
            window.buySuccess = false;
            return false;
        });
}

// commit

export function wireupCartButton(token){
    tkn = token;
    const viewCartButton = document.getElementById("btnShoppingBasket");
    if (viewCartButton.style.display === "none") {
        viewCartButton.style.display = "block";
    }
    viewCartButton.addEventListener('click', function() {
        getCart(tkn);
    });

}

// Function to delete an item from the cart
function deleteCartBundle(bundleId) {
    fetch('/api/removeFromCart', {
        method: 'DELETE',
        headers: {
            Authorization: 'Bearer ' + tkn
        },
        body: bundleId
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to delete item');
            }
            console.log('Item deleted successfully');
            getCart();
        })
        .catch(error => {
            console.error('Error deleting item:', error);
            getCart();
        });
}