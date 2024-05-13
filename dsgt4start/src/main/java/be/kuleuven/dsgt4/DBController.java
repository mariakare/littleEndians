package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    WebClient.Builder webClientBuilder;

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


    @GetMapping("/api/getBundles")
    public String getBundles() throws InterruptedException, ExecutionException {
        // for now return constant since adding bundles doesn't work yet:
        String jsonData = "{\n" +
                "  \"bundles\": [\n" +
                "    {\n" +
                "      \"id\": \"bundle1\",\n" +
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
                "      \"id\": \"bundle2\",\n" +
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

        /*

        //required level: user
        var user = WebSecurityConfig.getUser();

        WebClient webClient = webClientBuilder.build();
        String responseBody = webClient.get()
                .uri("http://localhost:8080/suits")
                .retrieve()
                .bodyToMono(String.class)
                .block();


        // Return the list of product data for all bundles in the response
        //return ResponseEntity.ok(allProductsData);
//        String type="";
//        String description="";
        try {
            // Convert JSON string to a Map or any other suitable data structure
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
            });

            // Add the data to Firestore
            db.collection("data").document("suits").set(data);

            //return ResponseEntity.ok("Data copied to Firestore successfully");
        } catch (IOException e) {
            e.printStackTrace();
            //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to copy data to Firestore");
        }


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
        return "";

         */
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
        WebClient webClient = webClientBuilder.build();

        StringBuilder jsonDataBuilder = new StringBuilder();
        jsonDataBuilder.append("{\n");
        jsonDataBuilder.append("  \"suppliers\": [\n");

        // Array of endpoint URLs
        String[] endpointURLs = {
                "http://localhost:8090/products",
                "http://localhost:8091/products",
                "http://localhost:8093/products"
        };

        // Loop through each endpoint
        for (String endpointURL : endpointURLs) {
            String responseBody = webClient.get()
                    .uri(endpointURL)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Extract supplier name from endpoint URL
            String supplierName = endpointURL.substring(endpointURL.lastIndexOf('/') + 1).toUpperCase();

            // Append supplier details to JSON
            jsonDataBuilder.append("    {\n");
            jsonDataBuilder.append("      \"name\": \"").append(supplierName).append("\",\n");
            jsonDataBuilder.append("      \"products\": [\n");

            // Process products from the response
            processProducts(responseBody, jsonDataBuilder);

            // Close products array and supplier object
            jsonDataBuilder.append("      ]\n");
            jsonDataBuilder.append("    }");

            // Add comma if there are more suppliers
            if (!endpointURL.equals(endpointURLs[endpointURLs.length - 1])) {
                jsonDataBuilder.append(",");
            }
            jsonDataBuilder.append("\n");
        }

        // Close suppliers array and JSON object
        jsonDataBuilder.append("  ]\n");
        jsonDataBuilder.append("}");

        // Print and return the JSON string
        String jsonString = jsonDataBuilder.toString();
        System.out.println(jsonString);
        return jsonString;
    }

    // Method to process products from the response and append to JSON
    private void processProducts(String responseBody, StringBuilder jsonDataBuilder) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode productListNode = rootNode.path("_embedded").path("productList");

            // Iterate over products and append to JSON
            for (JsonNode productNode : productListNode) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @PostMapping("/api/addBundle")
    public ResponseEntity<String> addBundle(
            @RequestParam("bundleTitle") String bundleTitle,
            @RequestParam("bundleDescription") String bundleDescription,
            @RequestParam("productIds") String productIds
    ) throws JsonProcessingException {
        var user = WebSecurityConfig.getUser();


        String productIdString = productIds.substring(1, productIds.length() - 1);
        String[] productIdSplit = productIdString.split(",");



        for (int i = 0; i < productIdSplit.length; i++) {
            productIdSplit[i] = productIdSplit[i].replaceAll("\"", "");
        }

        System.out.println(productIdSplit);

        // Create a map to hold the data for the new document
        Map<String, Object> data = new HashMap<>();
        data.put("name", bundleTitle);
        data.put("description", bundleDescription);
        data.put("productIds", Arrays.asList(productIdSplit));
        data.put("price", "$XX");

        // Process bundle data
        String response = "Bundle Title: " + bundleTitle + "\n" +
                "Bundle Description: " + bundleDescription + "\n" +
                "Selected Product Ids: " + productIds + "\n";

        try {

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


    @PostMapping("/api/updateBundle")
    public String updateBundle(
            @RequestParam("bundleId") String bundleId,
            @RequestParam("bundleTitle") String bundleTitle,
            @RequestParam("bundleDescription") String bundleDescription
    ) {
        System.out.println("I am in updateBundle");
        // Process updated bundle data
        String response = "Bundle ID: " + bundleId + "\n" +
                "Updated Bundle Title: " + bundleTitle + "\n" +
                "Updated Bundle Description: " + bundleDescription + "\n";

        System.out.println("Received updated bundle data:");
        System.out.println(response);

        return "Bundle updated successfully";
    }


}