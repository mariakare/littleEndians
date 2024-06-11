const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();
const db = admin.firestore();

exports.initializeFirestore = functions.https.onRequest(async (req, res) => {
    try {
        // Sample product data
        const product1 = {
            description: 'Look at this man go',
            imageLink: 'https://as2.ftcdn.net/v2/jpg/01/18/39/53/1000_F_118395300_KEO4hFI9FASizysdfpHnPhNuNuNpqvA0.jpg',
            name: 'mango',
            price: 500,
            supplier: 'littleEndians',
            amount: 30
        };

        const product2 = {
            description: 'greatly beloved by the littleEndians',
            imageLink: 'https://aliveplant.com/wp-content/uploads/2021/09/aphonso.jpeg',
            name: 'alfonso mango',
            price: 40,
            supplier: 'littleEndians',
            amount: 30
        };

        const product3 = {
            description: 'clothing brand for men',
            imageLink: 'https://s.yimg.com/uu/api/res/1.2/C3ISPlZxBRfb13CJXZ7lkg--~B/aD0xNjU0O3c9MjMzOTtzbT0xO2FwcGlkPXl0YWNoeW9u/http://media.zenfs.com/en_US/News/US-AFPRelax/mango_man.7bc5d111754.original.jpg',
            name: 'mango man',
            price: 500,
            supplier: 'littleEndians',
            amount: 30
        };

        const product4 = {
            description: 'a delicious refreshment',
            imageLink: 'https://goodtimein.co.uk/wp-content/uploads/2020/09/MANGOGO-250ml-Can-1080x1080px.jpg',
            name: 'mango go',
            price: 800,
            supplier: 'littleEndians',
            amount: 30
        };

        // Insert products
        await db.collection('products').doc().set(product1);
        await db.collection('products').doc().set(product2);
        await db.collection('products').doc().set(product3);
        await db.collection('products').doc().set(product4);

        res.status(200).send('Products inserted successfully');
    } catch (error) {
        console.error('Error inserting products:', error);
        res.status(500).send('Error inserting products');
    }
});

