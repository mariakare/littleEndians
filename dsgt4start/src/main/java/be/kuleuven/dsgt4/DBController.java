package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.api.core.ApiFuture;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import java.util.*;

@RestController
class DBController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    Firestore db;

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
            DocumentReference addedBundleRef = userRef.collection("basket").add(bundleData).get();
            // Wait for the result
            Map<String, Object> updatedBundleData = new HashMap<>();
            updatedBundleData.put("cartBundleId", addedBundleRef.getId());
            addedBundleRef.update(updatedBundleData);
            // Return a response
            return ResponseEntity.status(HttpStatus.CREATED).body("Bundle with ID: " + bundleId + " added to cart with ID: " + addedBundleRef.getId());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bundle with ID: " + bundleId + " does not exist");
        }
    }

    @GetMapping("/api/getBundles")
    public String getBundles() throws InterruptedException, ExecutionException {
        //required level: user
        var user = WebSecurityConfig.getUser();

//        WebClient webClient = webClientBuilder.build();
//        String responseBody = webClient.get()
//                .uri("http://localhost:8080/suits")
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//
//
//        // Return the list of product data for all bundles in the response
//        //return ResponseEntity.ok(allProductsData);
////        String type="";
////        String description="";
//        try {
//            // Convert JSON string to a Map or any other suitable data structure
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Object> data = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
//
//            // Add the data to Firestore
//            db.collection("data").document("suits").set(data);
//
//            //return ResponseEntity.ok("Data copied to Firestore successfully");
//        } catch (IOException e) {
//            e.printStackTrace();
//            //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to copy data to Firestore");
//        }




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
                    jsonDataBuilder.append("          \"description\": \"").append(productDescription).append("\"\n");
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
    public String getProducts() throws JsonProcessingException {
        // Required level: user
        var user = WebSecurityConfig.getUser();

        WebClient webClient = webClientBuilder.build();
        String responseBody1 = webClient.get()
                .uri("http://localhost:8090/products")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Modify the JSON response
        //ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder jsonDataBuilder = new StringBuilder();
        try {
            // Parse the response JSON from the WebClient
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody1);

            // Extract productList from the _embedded object
            JsonNode productListNode = rootNode.path("_embedded").path("productList");

            // StringBuilder to construct the JSON string

            jsonDataBuilder.append("{\n");
            jsonDataBuilder.append("  \"suppliers\": [\n");
            // Hardcode Supplier 1 details
            jsonDataBuilder.append("    {\n");
            jsonDataBuilder.append("      \"name\": \"Supplier 1\",\n");
            jsonDataBuilder.append("      \"products\": [\n");
            for (JsonNode productNode : productListNode) {
                // Extract product details
                String productName = productNode.path("name").asText();
                double productPrice = productNode.path("price").asDouble();
                String productDescription = productNode.path("description").asText();
                String imageLink = productNode.path("imageLink").asText();

                // Append product details to the JSON string
                jsonDataBuilder.append("        {\n");
                jsonDataBuilder.append("          \"id\": \"").append(productNode.path("id").asText()).append("\",\n");
                jsonDataBuilder.append("          \"name\":  \"").append(productName).append("\",\n");
                jsonDataBuilder.append("          \"price\": ").append(productPrice).append(",\n");
                jsonDataBuilder.append("          \"description\": \"").append(productDescription).append("\",\n");
                jsonDataBuilder.append("          \"imageLink\": \"").append(imageLink).append("\"\n");
                jsonDataBuilder.append("        },\n");
            }

            // Remove the trailing comma from the last product object
            if (productListNode.size() > 0) {
                jsonDataBuilder.deleteCharAt(jsonDataBuilder.length() - 2); // Removes the last comma
            }

            // Close products array and supplier 1 object
            jsonDataBuilder.append("      ]\n");
            jsonDataBuilder.append("    },\n");

            // Close suppliers array and JSON object
//            jsonDataBuilder.append("  ]\n");
//            jsonDataBuilder.append("}");

            // Return the JSON string
            //return jsonDataBuilder.toString();
            responseBody1=jsonDataBuilder.toString();

        } catch (Exception e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            //return null; // or return an error response
        }
        String responseBody2 = webClient.get()
                .uri("http://localhost:8091/products")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Modify the JSON response
        //ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder jsonDataBuilder2 = new StringBuilder();
        try {
            // Parse the response JSON from the WebClient
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody2);

            // Extract productList from the _embedded object
            JsonNode productListNode = rootNode.path("_embedded").path("productList");

            // StringBuilder to construct the JSON string


            // Hardcode Supplier 1 details
            jsonDataBuilder2.append("    {\n");
            jsonDataBuilder2.append("      \"name\": \"Supplier 2\",\n");
            jsonDataBuilder2.append("      \"products\": [\n");
            for (JsonNode productNode : productListNode) {
                // Extract product details
                String productName = productNode.path("name").asText();
                double productPrice = productNode.path("price").asDouble();
                String productDescription = productNode.path("description").asText();
                String imageLink = productNode.path("imageLink").asText();

                // Append product details to the JSON string
                jsonDataBuilder2.append("        {\n");
                jsonDataBuilder2.append("          \"id\": \"").append(productNode.path("id").asText()).append("\",\n");
                jsonDataBuilder2.append("          \"name\":  \"").append(productName).append("\",\n");
                jsonDataBuilder2.append("          \"price\": ").append(productPrice).append(",\n");
                jsonDataBuilder2.append("          \"description\": \"").append(productDescription).append("\",\n");
                jsonDataBuilder2.append("          \"imageLink\": \"").append(imageLink).append("\"\n");
                jsonDataBuilder2.append("        },\n");
            }

            // Remove the trailing comma from the last product object
            if (productListNode.size() > 0) {
                jsonDataBuilder2.deleteCharAt(jsonDataBuilder2.length() - 2); // Removes the last comma
            }

            // Close products array and supplier 1 object
            jsonDataBuilder2.append("      ]\n");
            jsonDataBuilder2.append("    },\n");

            // Close suppliers array and JSON object
//            jsonDataBuilder.append("  ]\n");
//            jsonDataBuilder.append("}");

            // Return the JSON string
            //return jsonDataBuilder.toString();
            responseBody2=jsonDataBuilder2.toString();

        } catch (Exception e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            //return null; // or return an error response
        }




        String responseBody3 = webClient.get()
                .uri("http://localhost:8093/products")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Modify the JSON response
        //ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder jsonDataBuilder3 = new StringBuilder();
        try {
            // Parse the response JSON from the WebClient
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody3);

            // Extract productList from the _embedded object
            JsonNode productListNode = rootNode.path("_embedded").path("productList");

            // StringBuilder to construct the JSON string


            // Hardcode Supplier 1 details
            jsonDataBuilder3.append("    {\n");
            jsonDataBuilder3.append("      \"name\": \"Supplier 3\",\n");
            jsonDataBuilder3.append("      \"products\": [\n");
            for (JsonNode productNode : productListNode) {
                // Extract product details
                String productName = productNode.path("name").asText();
                double productPrice = productNode.path("price").asDouble();
                String productDescription = productNode.path("description").asText();
                String imageLink = productNode.path("imageLink").asText();

                // Append product details to the JSON string
                jsonDataBuilder3.append("        {\n");
                jsonDataBuilder3.append("          \"id\": \"").append(productNode.path("id").asText()).append("\",\n");
                jsonDataBuilder3.append("          \"name\":  \"").append(productName).append("\",\n");
                jsonDataBuilder3.append("          \"price\": ").append(productPrice).append(",\n");
                jsonDataBuilder3.append("          \"description\": \"").append(productDescription).append("\",\n");
                jsonDataBuilder3.append("          \"imageLink\": \"").append(imageLink).append("\"\n");
                jsonDataBuilder3.append("        },\n");
            }

            // Remove the trailing comma from the last product object
            if (productListNode.size() > 0) {
                jsonDataBuilder3.deleteCharAt(jsonDataBuilder3.length() - 2); // Removes the last comma
            }

            // Close products array and supplier 1 object
            jsonDataBuilder3.append("      ]\n");
            jsonDataBuilder3.append("    }\n");
            jsonDataBuilder3.append("  ]\n");
            jsonDataBuilder3.append("}\n");

            // Close suppliers array and JSON object
//            jsonDataBuilder.append("  ]\n");
//            jsonDataBuilder.append("}");

            // Return the JSON string
            //return jsonDataBuilder.toString();
            responseBody3=jsonDataBuilder3.toString();

        } catch (Exception e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            //return null; // or return an error response
        }


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

        String json2="    {\n" +
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

        //return json;
        jsonDataBuilder.append(jsonDataBuilder2);
        jsonDataBuilder.append(jsonDataBuilder3);
        //jsonDataBuilder.append(json2);
        System.out.println(jsonDataBuilder.toString());
        return jsonDataBuilder.toString();
    }
}