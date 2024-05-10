package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.api.core.ApiFuture;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.ExecutionException;

import java.util.*;

@RestController
class DBController {

    private final WebClient.Builder webClientBuilder;

    @Autowired
    Firestore db;

    @Autowired
    public DBController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @PostMapping("/api/newUser")
    @ResponseBody
    public User newuser() {
        var user = WebSecurityConfig.getUser();

        Map<String, Object> data = new HashMap<>();
        data.put("user", user.getEmail());
        data.put("role", user.getRole());

        this.db.collection("user").document(user.getEmail().toString()).set(data);

        return user;
    }



    @GetMapping("/api/getBundles")
    public String getBundles() {
        //required level: user
        var user = WebSecurityConfig.getUser();

        /*
         * Everything in here is absolute bs
         * I am just using this as a temp to test a dynamic page
         */


        String jsonData = "{\n" +
                "  \"bundles\": [\n" +
                "    {\n" +
                "      \"name\": \"Bundle 1\",\n" +
                "      \"description\": \"Bundle 1 description goes here.\",\n" +
                "      \"products\": [\n" +
                "        {\n" +
                "          \"name\": \"Product 1\",\n" +
                "          \"description\": \"Short description for Product 1. Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes\",\n" +
                "          \"image\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"Product 2\",\n" +
                "          \"description\": \"Short description for Product 2.\",\n" +
                "          \"image\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"Product 3\",\n" +
                "          \"description\": \"Short description for Product 3.\",\n" +
                "          \"image\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Bundle 2\",\n" +
                "      \"description\": \"Bundle 2 description goes here.\",\n" +
                "      \"products\": [\n" +
                "        {\n" +
                "          \"name\": \"Product 4\",\n" +
                "          \"description\": \"Short description for Product 4.\",\n" +
                "          \"image\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"Product 5\",\n" +
                "          \"description\": \"Short description for Product 5.\",\n" +
                "          \"image\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"Product 6\",\n" +
                "          \"description\": \"Short description for Product 6.\",\n" +
                "          \"image\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        return jsonData;
    }

    @PostMapping("/api/addToCart")
    public ResponseEntity<String> addToCart(@RequestBody String bundleId) throws ExecutionException, InterruptedException {
        // Get the current user's ID
        var user = WebSecurityConfig.getUser();

        // Reference to the user's document
        DocumentReference userRef = db.collection("user").document(user.getEmail());

        // Reference to the bundle document
        DocumentReference bundleRef = db.collection("bundles").document(bundleId);

        // Get the bundle data
        ApiFuture<DocumentSnapshot> bundleFuture = bundleRef.get();
        DocumentSnapshot bundleSnapshot = bundleFuture.get();
        if (bundleSnapshot.exists()) {
            Map<String, Object> bundleData = new HashMap<>();
            bundleData.put("BundleId",bundleRef.getId());

            // Add the bundle document to the basket subcollection under the user's document
            DocumentReference addedBundleRef = userRef.collection("basket").add(bundleData).get();

            // Return a response
            return ResponseEntity.status(HttpStatus.CREATED).body("Bundle with ID: " + bundleId + " added to cart with ID: " + addedBundleRef.getId());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bundle with ID: " + bundleId + " does not exist");
        }
    }

    @GetMapping("/api/getCart")
    public List<Map<String, Object>> getCart() throws ExecutionException, InterruptedException {
        var user = WebSecurityConfig.getUser();

        // Reference to the user's document
        CollectionReference basketRef = db.collection("user").document(user.getEmail()).collection("basket");

        ApiFuture<QuerySnapshot> querySnapshot = basketRef.get();
        List<Map<String, Object>> shoppingCart = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Map<String, Object> itemData = document.getData();
            shoppingCart.add(itemData);
        }
        return shoppingCart;

    }

    @DeleteMapping("/api/removeFromCart")
    public ResponseEntity<String> removeFromCart(@RequestBody String bundleId) throws ExecutionException, InterruptedException {
        // Get the current user's ID
        var user = WebSecurityConfig.getUser();

        // Reference to the user's document
        CollectionReference userBasketRef = db.collection("user").document(user.getEmail()).collection("basket");

        DocumentReference bundleRef = userBasketRef.document(bundleId);


        bundleRef.delete();

        // Check if the document still exists after deletion
        boolean documentExists = bundleRef.get().get().exists();

        // Check if the deletion was successful
        if (!documentExists) {
            return ResponseEntity.status(HttpStatus.OK).body("Bundle with ID: " + bundleId + " removed from cart");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bundle with ID: " + bundleId + " does not exist");
        }
    }



    @GetMapping("/api/getProducts")
    public String getProducts() {
        // Required level: user
        /*
        var user = WebSecurityConfig.getUser();

        WebClient webClient = webClientBuilder.build();

        webClient.get()
                .uri("http://localhost:8080/suits")
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(response -> {
                    //processing
                });
    */

        // Dummy data
        String json = "{\n" +
                "  \"suppliers\": [\n" +
                "    {\n" +
                "      \"name\": \"Supplier 1\",\n" +
                "      \"products\": [\n" +
                "        {\n" +
                "          \"id\": 1,\n" +
                "          \"name\": \"Product A\",\n" +
                "          \"price\": 10.99,\n" +
                "          \"description\": \"Description of Product A\",\n" +
                "          \"imageLink\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 2,\n" +
                "          \"name\": \"Product B\",\n" +
                "          \"price\": 19.99,\n" +
                "          \"description\": \"Description of Product B\",\n" +
                "          \"imageLink\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 3,\n" +
                "          \"name\": \"Product C\",\n" +
                "          \"price\": 29.99,\n" +
                "          \"description\": \"Description of Product C\",\n" +
                "          \"imageLink\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Supplier 2\",\n" +
                "      \"products\": [\n" +
                "        {\n" +
                "          \"id\": 4,\n" +
                "          \"name\": \"Product D\",\n" +
                "          \"price\": 15.99,\n" +
                "          \"description\": \"Description of Product D\",\n" +
                "          \"imageLink\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 5,\n" +
                "          \"name\": \"Product E\",\n" +
                "          \"price\": 25.99,\n" +
                "          \"description\": \"Description of Product E\",\n" +
                "          \"imageLink\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 6,\n" +
                "          \"name\": \"Product F\",\n" +
                "          \"price\": 35.99,\n" +
                "          \"description\": \"Description of Product F\",\n" +
                "          \"imageLink\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Supplier 3\",\n" +
                "      \"products\": [\n" +
                "        {\n" +
                "          \"id\": 7,\n" +
                "          \"name\": \"Product G\",\n" +
                "          \"price\": 12.99,\n" +
                "          \"description\": \"Description of Product G\",\n" +
                "          \"imageLink\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 8,\n" +
                "          \"name\": \"Product H\",\n" +
                "          \"price\": 22.99,\n" +
                "          \"description\": \"Description of Product H\",\n" +
                "          \"imageLink\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 9,\n" +
                "          \"name\": \"Product I\",\n" +
                "          \"price\": 32.99,\n" +
                "          \"description\": \"Description of Product I\",\n" +
                "          \"imageLink\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        return json;
    }

    @PostMapping("/api/addBundle")
    public ResponseEntity<String> addNewBundle(@RequestParam Map<String,String> bundleData) throws ExecutionException, InterruptedException {
        // Get the current user's ID
        var user = WebSecurityConfig.getUser();

        String productIdArrString = bundleData.get("productIds");
        String productIdString = productIdArrString.substring(1, productIdArrString.length() - 1);
        String[] productIds = productIdString.split(",");


        for (int i = 0; i < productIds.length; i++) {
            productIds[i] = productIds[i].replaceAll("\"", "");
        }


        // Create a map to hold the data for the new document
        Map<String, Object> data = new HashMap<>();
        data.put("name", "New Bundle");
        data.put("productIds", Arrays.asList(productIds));

        try{

            DocumentReference bundleRef = db.collection("bundles").document();


            // Set the data for the new document
            ApiFuture<WriteResult> writeResult = bundleRef.set(data);
            // Wait for the operation to complete
            writeResult.get();

            // Retrieve the Firestore-generated ID of the new document
            String bundleId = bundleRef.getId();

            // Return a success response with the ID of the newly created document
            return ResponseEntity.status(HttpStatus.CREATED).body("Bundle created with ID: " + bundleId);
        } catch (Exception e) {
            // Handle any exceptions that might occur during the operation
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating document: " + e.getMessage());
        }

    }

}