package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.api.core.ApiFuture;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.*;
import java.util.*;

@RestController
class DBController {

    @Autowired
    WebClient.Builder webClientBuilder;

    @Autowired
    Firestore db;

    @PostMapping("/api/newUser")
    @ResponseBody
    public User newuser() throws InterruptedException, ExecutionException {
        var user = WebSecurityConfig.getUser();

        Map<String, Object> data = new HashMap<>();
        data.put("user", user.getEmail().toString());
        data.put("role", user.getRole().toString());

        this.db.collection("user").document(user.getEmail().toString()).set(data);

        return user;
    }



    @GetMapping("/api/getBundles")
    public String getBundles() throws InterruptedException, ExecutionException {
        //required level: user
        var user = WebSecurityConfig.getUser();



        // Return the list of product data for all bundles in the response
        //return ResponseEntity.ok(allProductsData);
//        String type="";
//        String description="";



        try {
            // Reference to the bundles collection in Firestore
            CollectionReference bundlesRef = this.db.collection("bundle");

            // Query to retrieve all documents in the bundles collection
            Query query = bundlesRef;

            // Execute the query and retrieve all bundle documents
            QuerySnapshot querySnapshot = query.get().get();

            // StringBuilder to construct the JSON string
            StringBuilder jsonDataBuilder = new StringBuilder();
            jsonDataBuilder.append("{\n");
            jsonDataBuilder.append("  \"bundles\": [\n");


            // Iterate over each document in the query result
            for (QueryDocumentSnapshot document : querySnapshot) {
                // Extract data from the document
                String name = document.getString("name");
                String description = document.getString("description");

                // Append bundle details to the JSON string
                jsonDataBuilder.append("    {\n");
                jsonDataBuilder.append("      \"name\": \"").append(name).append("\",\n");
                jsonDataBuilder.append("      \"description\": \"").append(description).append("\",\n");
                jsonDataBuilder.append("      \"products\": [\n");

                // Extract product references map from the document
//                GenericTypeIndicator<Map<String, DocumentReference>> typeIndicator = new GenericTypeIndicator<Map<String, DocumentReference>>() {};
//                Map<String, DocumentReference> productReferences = document.get("products", typeIndicator);
                Map<String, Object> productReferences = (Map<String, Object>) document.get("products");


                // Iterate over each product reference in the map
                for (Map.Entry<String, Object> entry : productReferences.entrySet()) {
                    // Retrieve product document reference
                    DocumentReference productRef = (DocumentReference) entry.getValue();

                    // Fetch product document from Firestore
                    DocumentSnapshot productSnapshot = productRef.get().get();

                    // Extract product data from the product document
                    String productType = productSnapshot.getString("name");
                    String productDescription = productSnapshot.getString("description");

                    // Append product details to the JSON string
                    jsonDataBuilder.append("        {\n");
                    jsonDataBuilder.append("          \"name\": \"").append(productType).append("\",\n");
                    jsonDataBuilder.append("          \"description\": \"").append(productDescription).append("\",\n");
                    jsonDataBuilder.append("          \"image\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSJqSUCfOuELtH0u5rBpf1Lnzy1Xp0lZgsblRa-mEM8_Q&s\"\n");
                    jsonDataBuilder.append("        },\n");
                }

                // Remove the trailing comma from the last product object
                if (!productReferences.isEmpty()) {
                    jsonDataBuilder.deleteCharAt(jsonDataBuilder.length() - 2); // Removes the last comma
                }

                // Append closing brackets for products array and bundle object
                jsonDataBuilder.append("      ]\n");
                jsonDataBuilder.append("    },\n");
            }

            // Remove the trailing comma from the last bundle object
            if (!querySnapshot.isEmpty()) {
                jsonDataBuilder.deleteCharAt(jsonDataBuilder.length() - 2); // Removes the last comma
            }

            // Append closing brackets for bundles array and JSON object
            jsonDataBuilder.append("  ]\n");
            jsonDataBuilder.append("}");

            // Return the JSON string in the response
            //return ResponseEntity.ok(jsonDataBuilder.toString());
            return jsonDataBuilder.toString();
        } catch (InterruptedException | ExecutionException e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }



        String jsonData = "{\n" +
                "  \"bundles\": [\n" +
                "    {\n" +
                "      \"name\": \"Bundle 1\",\n" +
                "      \"description\": \"Bundle 1 description goes here.\",\n" +
                "      \"products\": [\n" +
                "        {\n" +
                "          \"name\":  \"Product 1\",\n" +
                "          \"description\": \"Short description for Product 1.\",\n" +
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
            Map<String, Object> bundleData = bundleSnapshot.getData();
            // Add the bundle document to the basket subcollection under the user's document
            ApiFuture<DocumentReference> future = userRef.collection("basket").add(bundleData);
            // Wait for the result
            DocumentReference addedBundleRef = future.get();
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
}