let tkn;

export function setToken(token){tkn = token}

export function getCart() {

    fetch('/api/getCart', {
        headers: { Authorization: 'Bearer ' + tkn}
    })
        .then((response) => {
            return response.json();
        })
        .then((data) => {
            console.log(data);
            // // displayShoppingCart(data);
            displayCartPage(data);
        })
        .catch(function (error) {
            console.log("bro is this it?")
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

    displaySec(data["cart"] || [], "Cart", true, true);
    // displaySec(data["shipping"] || [], "Orders on their way to you", false, false);
    displaySec(data["past"] || [], "Bought", false, false);
}

let crt = [];

function displaySec(data, head, clear, isCart){
    const contentDiv = document.getElementById('contentdiv');
    if (clear){
        contentDiv.innerHTML = '';
        console.log("Here you see cart data:");
        console.log(data);
        crt = []
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
            crt.push(item.id);
            const buyButton = document.createElement('button');
            buyButton.textContent = 'Buy';
            buyButton.classList.add('buy-button');
            buyButton.addEventListener('click', function() {
                // Handle buy action
                console.log(item);
                buyBundle(item.id); // Pass item.bundleId to buyBundle
            });
            itemDiv.appendChild(buyButton);

            // Create a delete button for each item
            const deleteButton = document.createElement('button');
            deleteButton.textContent = 'Delete';
            deleteButton.classList.add('delete-button');
            deleteButton.addEventListener('click', function() {
                // Handle delete action
                console.log(item);
                deleteCartBundle(item.id); // This is correct
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



// function buyAll(){
//     const buyButtons = document.querySelectorAll('.cart-item .buy-button');
//     const promises = [];
//
//     crt.forEach(bundleId => {
//         promises.push(buyBundle(bundleId, true));
//     });
//
//     Promise.allSettled(promises)
//         .then(results => {
//             const failedBuys = results.filter(result => result.status === "rejected");
//             if (failedBuys.length > 0) {
//                 const failedItems = failedBuys.length === 1 ? 'item' : 'items';
//                 alert(`Failed to buy ${failedBuys.length} ${failedItems}. Please try again.`);
//             } else {
//                 alert('All items bought successfully!');
//             }
//         })
//         .catch(error => {
//             console.error('Error during the buy process:', error);
//             alert('Some items could not be bought. Please try again.');
//         });
// }







// function buyBundle(bundleId, silent = false) {
//     return fetch('/api/buyBundle', {
//         method: 'POST',
//         headers: {
//             'Content-Type': 'application/json',
//             Authorization: 'Bearer ' + tkn
//         },
//         body: JSON.stringify({ bundleId: bundleId })
//     })
//         .then(response => {
//             if (response.status === 400) {
//                 return response.text().then(errorMessage => {
//                     // Display a popup to the user indicating failure
//                     if(!silent){
//                         alert(errorMessage);
//                     }
//                     return false; // Indicate failure
//                 });
//             }
//
//             if (!response.ok) {
//                 throw new Error('Failed to buy item');
//             }
//             console.log('Item bought successfully');
//             getCart(); // Refresh the cart after buying an item
//             window.buySuccess = true;
//             return true;
//         })
//         .catch(error => {
//             console.error('Error buying item:', error);
//             getCart(); // Refresh the cart even if there's an error
//             window.buySuccess = false;
//             return false;
//         });
// }






function buyBundle(bundleId, silent = false) {
    return fetch('/api/buyBundle', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: 'Bearer ' + tkn
        },
        body: JSON.stringify({ bundleId: bundleId })
    })
        .then(response => {
            if (response.status === 400) {
                return response.text().then(errorMessage => {
                    // Display a popup to the user indicating failure
                    if (!silent) {
                        alert(errorMessage);
                    }
                    return false; // Indicate failure
                });
            }

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

function buyAll() {
    const buyButtons = document.querySelectorAll('.cart-item .buy-button');
    const promises = [];

    crt.forEach(bundleId => {
        promises.push(buyBundle(bundleId, true));
    });

    Promise.all(promises)
        .then(results => {
            const failedBuys = results.filter(result => !result);
            if (failedBuys.length > 0) {
                const failedItems = failedBuys.length === 1 ? 'item' : 'items';
                alert(`Failed to buy ${failedBuys.length} ${failedItems}. Please try again.`);
            } else {
                alert('All items bought successfully!');
            }
        })
        .catch(error => {
            console.error('Error during the buy process:', error);
            alert('Some items could not be bought. Please try again.');
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