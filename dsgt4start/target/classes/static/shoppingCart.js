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
            displayShoppingCart(data);
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

// commit

export function wireupCartButton(token){
    tkn = token;
    document.getElementById('btnShoppingBasket').addEventListener('click', function() {
        getCart(tkn);
    });


}