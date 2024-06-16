package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.*;
import com.google.gson.Gson;
import net.minidev.json.JSONObject;
import org.eclipse.jetty.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.api.core.ApiFuture;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import java.util.*;

@RestController
class DBController {

    String apiToken="Iw8zeveVyaPNWonPNaU0213uw3g6Ei";
    String headerValue = "Authorization: Bearer Iw8zeveVyaPNWonPNaU0213uw3g6Ei";




    @Autowired
    WebClient.Builder webClientBuilder;

    @Autowired
    Firestore db;



    @PostMapping("/api/newUser")
    @ResponseBody
    public User newuser() throws ExecutionException, InterruptedException {
        var user = WebSecurityConfig.getUser();



        Map<String, Object> data = new HashMap<>();
        data.put("user", user.getEmail());
        data.put("role", user.getRole());

        this.db.collection("user").document(user.getEmail().toString()).set(data);

        return user;
    }


    @GetMapping("/api/getBundles")
    public String getBundles() throws InterruptedException, ExecutionException {
        var user = WebSecurityConfig.getUser();

        try {
            // Reference to the bundles collection in Firestore
            CollectionReference bundlesRef = this.db.collection("bundles");

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
                String price = document.getDouble("price").toString();
                String id = document.getString("id");

                // Append bundle details to the JSON string
                jsonDataBuilder.append("    {\n");
                jsonDataBuilder.append("      \"name\": \"").append(name).append("\",\n");
                jsonDataBuilder.append("      \"description\": \"").append(description).append("\",\n");
                jsonDataBuilder.append("      \"id\": \"").append(id).append("\",\n");
                jsonDataBuilder.append("      \"price\": \"").append(price).append("\",\n");
                jsonDataBuilder.append("      \"products\": [\n");


                // Extract product IDs from the document
                List<DocumentReference> productRefs = (List<DocumentReference>) document.get("productIds");
                //List<DocumentReference> productIds = new ArrayList<>();
                /*
                for (DocumentReference productRef : productRefs) {
                    productIds.add(productRef.getId());
                }
                */
                //int i=0;
                for (DocumentReference productRef : productRefs) {
                    // Fetch product document from Firestore
                    String productType = null;
                    String productDescription = null;
                    Double productPrice = null;
                    String imageLink = null;

                    // Retrieve product data directly from Firestore
                    //DocumentReference productRef = this.db.collection("products").document(productId);
                    DocumentSnapshot productSnapshot = productRef.get().get();
                    if (productSnapshot.exists()) {
                        // Extract product data from the product document
                        productType = productSnapshot.getString("name");
                        productDescription = productSnapshot.getString("description");
                        productPrice = productSnapshot.getDouble("price");
                        imageLink = productSnapshot.getString("imageLink");
                    } else {
                        System.out.println("product does not exist");
                    }

                    // Append product details to the JSON string
                    jsonDataBuilder.append("        {\n");
                    jsonDataBuilder.append("          \"name\": \"").append(productType).append("\",\n");
                    jsonDataBuilder.append("          \"description\": \"").append(productDescription).append("\",\n");
                    jsonDataBuilder.append("          \"price\": \"").append(productPrice).append("\",\n");
                    jsonDataBuilder.append("          \"imageLink\": \"").append(imageLink).append("\"\n");
                    jsonDataBuilder.append("        },\n");

                }
                //i=0;

                // Remove the trailing comma from the last product object
                if (!productRefs.isEmpty()) {
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
            System.out.println(jsonDataBuilder.toString());
            return jsonDataBuilder.toString();
        } catch (InterruptedException | ExecutionException e) {
            // Handle exceptions appropriately
            e.printStackTrace();
            // Return an error response
            return "{\"error\": \"Failed to retrieve bundles\"}";
        }
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
//            String fieldValue = bundleSnapshot.getString("id");
            Map<String, Object> data = new HashMap<>();
            data.put("bundleRef", bundleRef);
            data.put("id", "");
//
//            // Add the bundle document to the basket subcollection under the user's document
//            DocumentReference addedBundleRef = userRef.collection("basket").add(bundleData).get();
//            // Wait for the result
//            Map<String, Object> updatedBundleData = new HashMap<>();
//            updatedBundleData.put("cartBundleId", addedBundleRef.getId());
//            addedBundleRef.update(updatedBundleData);
//            // Return a response
            try {

                DocumentReference cartBundleRef = userRef.collection("basket").document();


                // Set the data for the new document
                ApiFuture<WriteResult> writeResult = cartBundleRef.set(data);
                writeResult.get();

                // Retrieve the Firestore-generated ID of the new document
                String cartId = cartBundleRef.getId();

                ApiFuture<WriteResult> updateFuture = cartBundleRef.update("id", cartId);
                updateFuture.get();

                // Return a success response with the ID of the newly created document
                return ResponseEntity.status(HttpStatus.CREATED).body("Bundle created with ID: " + cartId);
            } catch (Exception e) {
                // Handle any exceptions that might occur during the operation
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating document: " + e.getMessage());
            }
            //return ResponseEntity.status(HttpStatus.CREATED).body("Bundle with ID: " + bundleId + " added to cart with ID: " + BundleRef.getId());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bundle with ID: " + bundleId + " does not exist");
        }
    }


    @GetMapping("/api/getCart")
    public String getCart() throws ExecutionException, InterruptedException {
        var user = WebSecurityConfig.getUser();

        Map<String, List<Map<String, Object>>> cartData = new HashMap<>();
        cartData.put("cart", new ArrayList<>()); // Renamed to "cart" to match JS
        cartData.put("shipping", new ArrayList<>()); // Renamed to "shipping" to match JS
        cartData.put("past", new ArrayList<>()); // Renamed to "past" to match JS

        CollectionReference basketRef = db.collection("user").document(user.getEmail()).collection("basket");
        cartData.get("cart").addAll(getCartItemsFromCollection(basketRef, user.getEmail()));

        CollectionReference processingRef = db.collection("user").document(user.getEmail()).collection("processing");
        cartData.get("shipping").addAll(getCartItemsFromCollection(processingRef, user.getEmail()));

        CollectionReference orderedRef = db.collection("user").document(user.getEmail()).collection("ordered");
        cartData.get("past").addAll(getCartItemsFromCollection(orderedRef, user.getEmail()));

        Gson gson = new Gson();
        String json = gson.toJson(cartData);

        System.out.println(json);
        return json;
    }

    private List<Map<String, Object>> getCartItemsFromCollection(CollectionReference collectionRef, String userId)
            throws ExecutionException, InterruptedException {
        List<Map<String, Object>> cartItems = new ArrayList<>();

        ApiFuture<QuerySnapshot> snapshotFuture = collectionRef.get();
        QuerySnapshot snapshot = snapshotFuture.get();
        for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("id", document.getString("id"));
            itemData.put("userId", userId);

            DocumentReference bundleRef = (DocumentReference) document.get("bundleRef");
            if (bundleRef != null) {
                DocumentSnapshot bundleSnapshot = bundleRef.get().get();
                if (bundleSnapshot.exists()) {
                    String bundleId = bundleSnapshot.getString("id");
                    itemData.put("bundleId", bundleId); // Use 'bundleId' instead of 'bundleRef'
                    itemData.put("name", bundleSnapshot.getString("name"));
                } else {
                    System.out.println("Bundle does not exist for item: " + document.getId());
                }
            } else {
                System.out.println("bundleRef is null for item: " + document.getId());
            }
            cartItems.add(itemData);
        }

        return cartItems;
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
    @PreAuthorize("hasRole('manager')")
    public String getProducts() throws JsonProcessingException {

        WebClient webClient = webClientBuilder.build();

        Gson gson = new Gson();

        StringBuilder jsonDataBuilder = new StringBuilder();
        jsonDataBuilder.append("{\n");
        jsonDataBuilder.append("  \"suppliers\": [\n");

        // Array of endpoint URLs
        String[] endpointURLs = {
                "http://sud.switzerlandnorth.cloudapp.azure.com:8090/products/",
                "http://ivan.canadacentral.cloudapp.azure.com:8091/products/",
                "http://sud.japaneast.cloudapp.azure.com:8093/products/"
        };


        // Loop through each endpoint
        for (String endpointURL : endpointURLs) {
            String responseBody = webClient.get()
                    .uri(endpointURL)
                    .header("Authorization", "Bearer " + apiToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Extract supplier name from endpoint URL
            String supplierName = endpointURL.substring(0,endpointURL.lastIndexOf('/') + 1);

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
//            if (!endpointURL.equals(endpointURLs[endpointURLs.length - 1])) {
//                jsonDataBuilder.append(",");
//            }
            jsonDataBuilder.append(",");
            jsonDataBuilder.append("\n");
        }

        jsonDataBuilder.append("    {\n");
        jsonDataBuilder.append("      \"name\": \"Supplier 4\",\n");
        jsonDataBuilder.append("      \"products\": [\n");


        List<Map<String, Object>> products = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> query = db.collection("products").get();
            for (DocumentSnapshot document : query.get().getDocuments()) {

                if(document.get("supplier").equals("littleEndians")) {

                    Map<String, Object> product = document.getData();
                    // Adjust the Firestore data structure to match the external JSON structure
                    Map<String, Object> formattedProduct = new HashMap<>();
                    formattedProduct.put("id", document.getId()); // Assuming Firestore uses auto-generated IDs
                    formattedProduct.put("name", product.get("name"));
                    formattedProduct.put("price", product.get("price"));
                    formattedProduct.put("description", product.get("description"));
                    formattedProduct.put("imageLink", product.get("imageLink"));

                    products.add(formattedProduct);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error retrieving products from Firestore: " + e);
        }




        for (int i = 0; i < products.size(); i++) {
            Map<String, Object> product = products.get(i);
            jsonDataBuilder.append(gson.toJson(product));

            if (i < products.size() - 1) {
                jsonDataBuilder.append(",");
            }
            jsonDataBuilder.append("\n");
        }

        // Close products array and supplier object
        jsonDataBuilder.append("      ]\n"); // This was missing!
        jsonDataBuilder.append("    }");

        // Close suppliers array and JSON object
        jsonDataBuilder.append("  ]\n");
        jsonDataBuilder.append("}");

        // ... (Code to close the suppliers array and JSON object remains the same) ...

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

    private Map<DocumentReference, Double> addProduct(String[] productIds){
        //CollectionReference products = db.collection("products");
        WebClient webClient = webClientBuilder.build();
        int i=0;

        List<DocumentReference> documentReferences = new ArrayList<>();
        Map<DocumentReference, Double> documentMap = new HashMap<>();

        for (String id: productIds){

            String[] idParts = id.split("@");

            DocumentReference docRef = db.collection("products").document(idParts[1]);
            //System.out.println("TESTINGGG");

            ApiFuture<DocumentSnapshot> future = docRef.get();
            try {
                double price=0;
                // Get the document snapshot
                DocumentSnapshot document = future.get();

                // Check if the document exists
                if (!document.exists()) {

                    String endpointURL=idParts[0]+idParts[1];
                    System.out.println(endpointURL);

                    String responseBody = webClient.get()
                            .uri(endpointURL)
                            .header("Authorization", "Bearer " + apiToken)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    System.out.println(responseBody);


                    try{

                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(responseBody);

                        Map<String, Object> data = new HashMap<>();
                        // Add data to the document as needed
                        data.put("name", rootNode.path("name").asText());
                        data.put("description", rootNode.path("description").asText());
                        data.put("imageLink", rootNode.path("imageLink").asText());
                        data.put("supplier", idParts[0].substring(0, idParts[0].length() - "products/".length()));
                        data.put("price", rootNode.path("price").asDouble());

                        price=rootNode.path("price").asDouble();

                        ApiFuture<WriteResult> result = docRef.set(data);

                        // Wait for the set operation to complete
                        result.get();
                        System.out.println("Document created!");

                    }catch(Exception e) {
                        // Handle any exceptions that might occur during the operation
                        e.printStackTrace();
                        //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating document: " + e.getMessage());
                    }
                }
                else{
                    price = document.getDouble("price");
                }
                documentReferences.add(docRef);
                documentMap.put(docRef, price);

            } catch (InterruptedException | ExecutionException e) {
                // Handle any errors that may occur
                System.err.println("Error getting document: " + e.getMessage());
            }

            i++;
        }
        return documentMap;
    }



    @PostMapping("/api/addBundle")
    @PreAuthorize("hasRole('manager')")
    public ResponseEntity<String> addBundle(
            @RequestParam("bundleTitle") String bundleTitle,
            @RequestParam("bundleDescription") String bundleDescription,
            @RequestParam("productIds") String productIds
    ) throws JsonProcessingException, ExecutionException, InterruptedException {
        var user = WebSecurityConfig.getUser();


        String productIdString = productIds.substring(1, productIds.length() - 1);
        String[] productIdSplit = productIdString.split(",");




        for (int i = 0; i < productIdSplit.length; i++) {
            productIdSplit[i] = productIdSplit[i].replaceAll("\"", "");
        }

        Map<DocumentReference, Double> products = addProduct(productIdSplit);

        List<DocumentReference> productIdFinal=new ArrayList<>(products.keySet());

        double totalPrice = 0.0;
        for (Double value : products.values()) {
            totalPrice += value;
        }

        // Create a map to hold the data for the new document
        Map<String, Object> data = new HashMap<>();
        data.put("id", "");
        data.put("name", bundleTitle);
        data.put("description", bundleDescription);
        data.put("productIds", productIdFinal);
        data.put("price", totalPrice*0.9);//add 10 precent discount

        // Process bundle data
        String response = "Bundle Title: " + bundleTitle + "\n" +
                "Bundle Description: " + bundleDescription + "\n" +
                "Selected Product Ids: " + productIds + "\n";

        try {

            DocumentReference bundleRef = db.collection("bundles").document();


            // Set the data for the new document
            ApiFuture<WriteResult> writeResult = bundleRef.set(data);
            writeResult.get();

            // Retrieve the Firestore-generated ID of the new document
            String bundleId = bundleRef.getId();

            ApiFuture<WriteResult> updateFuture = bundleRef.update("id", bundleId);
            updateFuture.get();

            // Return a success response with the ID of the newly created document
            return ResponseEntity.status(HttpStatus.CREATED).body("Bundle created with ID: " + bundleId);
        } catch (Exception e) {
            // Handle any exceptions that might occur during the operation
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating document: " + e.getMessage());
        }

    }


    @PostMapping("/api/updateBundle")
    @PreAuthorize("hasRole('manager')")
    public String updateBundle(
            @RequestParam("bundleId") String bundleId,
            @RequestParam("bundleTitle") String bundleTitle,
            @RequestParam("bundleDescription") String bundleDescription
    ) {
        System.out.println("I am in updateBundle");
        //bundleId="mnLObSBKMBZGJ8UzHTrI";//TODO: This needs to be gone
        // Process updated bundle data
        String response = "Bundle ID: " + bundleId + "\n" +
                "Updated Bundle Title: " + bundleTitle + "\n" +
                "Updated Bundle Description: " + bundleDescription + "\n";

        System.out.println("Received updated bundle data:");
        System.out.println(response);

        try {
            // Reference to the bundle document in Firestore
            DocumentReference bundleRef = db.collection("bundles").document(bundleId);

            // Fetch the bundle document
            DocumentSnapshot bundleSnapshot = bundleRef.get().get();

            if (bundleSnapshot.exists()) {
                // Update the bundle variables
                bundleRef.update(
                        "name", bundleTitle,
                        "description", bundleDescription
                );

                System.out.println("yes");

                return "Bundle updated successfully";
            } else {

                System.out.println("no");
                return "Bundle with ID " + bundleId + " does not exist";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to update bundle";
        }
    }

    @DeleteMapping("/api/deleteBundle/{bundleId}")
    @PreAuthorize("hasRole('manager')")
    public String deleteBundle(@PathVariable String bundleId) {
        System.out.println("I am in deleteBundle");
        //bundleId="3eWJgwFYXJYh8P8caPtO";

        try {
            // Reference to the bundle document in Firestore
            DocumentReference bundleRef = db.collection("bundles").document(bundleId);

            // Fetch the bundle document
            DocumentSnapshot bundleSnapshot = bundleRef.get().get();

            if (bundleSnapshot.exists()) {
                // Get the list of product references in the bundle
                List<DocumentReference> productRefs = (List<DocumentReference>) bundleSnapshot.get("productIds");
                List<String> productIds = new ArrayList<>();

                // Extract product IDs from product references
                for (DocumentReference productRef : productRefs) {
                    productIds.add(productRef.getId());
                }

                bundleRef.delete();


                // Loop through product IDs
                for (String productId : productIds) {
                    boolean isLocal=false;
                    DocumentReference productRef = db.collection("products").document(productId);

                    DocumentSnapshot productSnapshot = productRef.get().get();
                    String supplier=productSnapshot.getString("supplier");

                    if(supplier.equals("littleEndians")){
                        isLocal=true;
                    }

                    // Query all bundles except the current one to check for references to the product
                    Query query = db.collection("bundles").whereArrayContains("productIds", db.document("products/" + productId));
                    ApiFuture<QuerySnapshot> querySnapshot = query.get();

                    // If no other bundle except the current one references the product, delete it
                    if (querySnapshot.get().isEmpty() && !isLocal) {
                        db.collection("products").document(productId).delete();
                        System.out.println("Product with ID " + productId + " deleted successfully");
                    }
                }

                System.out.println("Bundle deleted successfully");

                return "Bundle deleted successfully";
            } else {
                System.out.println("Bundle with ID " + bundleId + " does not exist");
                return "Bundle with ID " + bundleId + " does not exist";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to delete bundle";
        }
    }

    @PostMapping("/api/buyBundle")
    public ResponseEntity<String> sendReservation(@RequestBody String jsonString) throws InterruptedException, ExecutionException {
        System.out.println("i am in reserve");
        System.out.println(jsonString);

        int startIndex = jsonString.indexOf(':') + 2;

// Find the ending index of the value (assuming the string is well-formed JSON)
        int endIndex = jsonString.length() - 2; // Subtract 1 to exclude the closing curly brace

// Extract the value as a substring
        String bundleId = jsonString.substring(startIndex, endIndex).trim(); // Trim leading/trailing spaces



        System.out.println("Bundle ID: " + bundleId);
        var user = WebSecurityConfig.getUser();
        boolean isSuccesful=true;
        Map<String, String> reservations = new HashMap<>();

        //System.out.println("Bundle ID: " + bundleId);

        // Array of endpoint URLs
        WebClient webClient = webClientBuilder.build();
//        String[] endpointURLs = {
//                "http://sud.switzerlandnorth.cloudapp.azure.com:8080/reservations/",
//                "http://ivan.canadacentral.cloudapp.azure.com:8080/reservations/",
//                "http://sud.japaneast.cloudapp.azure.com:8080/reservations/"
//        };
        System.out.println("before");
        DocumentReference orderRef = db.collection("user").document(user.getEmail()).collection("basket").document(bundleId);
        System.out.println("after");
        ApiFuture<DocumentSnapshot> future = orderRef.get();
        DocumentSnapshot orderSnap;
        try {
            System.out.println("yes");

            orderSnap = future.get();
            System.out.println("yesyes");
            //System.out.println(orderSnap.getData());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("bundle doesn't exist");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("bundle doesn't exist.");
        }
        System.out.println("yesyesyes");

        if (orderSnap.exists()) {

            System.out.println("we are going well");

            //System.out.println("WTF is going on???");
            // Get the DocumentReference from the "bundleRef" field
            DocumentReference bundleRef = (DocumentReference) orderSnap.get("bundleRef");

            DocumentSnapshot bundleSnapshot = bundleRef.get().get();
            List<DocumentReference> productRefs = (List<DocumentReference>) bundleSnapshot.get("productIds");

            for (DocumentReference productRef : productRefs) {
                String productId = productRef.getId();
                DocumentReference productDocRef = db.collection("products").document(productId);
                DocumentSnapshot productSnapshot = productDocRef.get().get();

                String supplierUrl = (String) productSnapshot.get("supplier");

                if (supplierUrl.equals("littleEndians")) {

                    DocumentSnapshot productDoc = productDocRef.get().get();

                    // Check if the amount is greater than zero
                    if (productDoc.get("amount") != null && (long) productDoc.get("amount") > 0) {
                        // Decrement the amount
                        long currentAmount = (long) productDoc.get("amount");
                        long newAmount = currentAmount - 1;
                        productDocRef.update("amount", newAmount);

                        // Add a reservation document to the 'reservations' collection
                        DocumentReference reservationRef = db.collection("reservations").document();

                        Map<String, Object> reservationData = new HashMap<>();
                        reservationData.put("productRef", productRef); // Add the product reference
                        reservationData.put("status", "PENDING");

                        reservationRef.set(reservationData);
                        String reservationId = reservationRef.getId();
                        reservations.put(reservationId, "littleEndians");
                    }
                    else{
                        System.out.println("reserving in own products failed");
                        isSuccesful=false;
                        break;
                    }


                }

                else{
                    String finalUrl = supplierUrl + "products/reserve";
                    System.out.println(finalUrl);

                    Map<String, Integer> productsToReserve = new HashMap<>();
                    productsToReserve.put(productId, 1);
                    try {

                        Map responseBody = webClient.post()
                                .uri(finalUrl)
                                .header("Authorization", "Bearer " + apiToken)
                                .body(BodyInserters.fromObject(productsToReserve))
                                .retrieve()
                                .bodyToMono(Map.class)
                                .block();
                        // ... (process successful response - optional)

                        System.out.println(responseBody.toString());

                        boolean isSuccessful = responseBody.get("status").equals("PENDING");
                        String reservationId = (String) responseBody.get("reservationId");
                        reservations.put(reservationId, supplierUrl);
                        if(!isSuccessful){
                            isSuccesful=false;


                        }
                    } catch (Exception e) {
                        // Handle exception within the thread (e.g., log the error)
                        System.out.println("error in reservation of"+ productId);
                        isSuccesful=false;

                    }

                }






            }



            if(isSuccesful){
                System.out.println("Bundle reserved successfully");
                moveBundle(bundleId, "basket", "processing", user.getEmail(), reservations);
                buyBundle(reservations, bundleId, user.getEmail());
                return ResponseEntity.ok("Bundle has been reserved");
            }

            else{
                cancelBundle(reservations);
                System.out.println("Bundle was not reserved successfully:((((( Initiate self-destruct protocol");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to reserve.");


            }

        }
        else{
            System.out.println("wtf why doens't it exist");
        }


        // send reservation per bundle
        // [add supplier field to product later]
        // return bundle reference
        return ResponseEntity.ok("Bundle has been reserved");

    }

    public String cancelBundle(Map<String, String> reservations){
        WebClient webClient = webClientBuilder.build();

        try {

            if (!reservations.isEmpty()) {
                for (Map.Entry<String, String> entry : reservations.entrySet())  {
                    String reservationId = entry.getKey();
                    String url = entry.getValue();

                    if(url.equals("littleEndians")){
                        System.out.println("We are canceling our own product");

                        DocumentReference reservationRef = db.collection("reservations").document(reservationId);
                        DocumentSnapshot reservationDoc = reservationRef.get().get();

                        DocumentReference productRef = (DocumentReference) reservationDoc.get("productRef");

                        DocumentReference productDocRef = db.collection("products").document(productRef.getId());
                        DocumentSnapshot productDoc = productDocRef.get().get();

                        if (productDoc.get("amount") != null) {

                            long currentAmount = (long) productDoc.get("amount");
                            long newAmount = currentAmount + 1;
                            productDocRef.update("amount", newAmount);


                            reservationRef.delete();
                            System.out.println("Reservation " + reservationId + " cancelled and product amount updated");
                        } else {
                            System.err.println("bruh.");
                        }

                    }
                    else{
                        String finalUrl = url + "reservations/" + reservationId + "/cancel";
                        System.out.println(finalUrl);
                        try {
                            Map responseBody = webClient.post()
                                    .uri(finalUrl)
                                    .header("Authorization", "Bearer " + apiToken)
                                    .retrieve()
                                    .bodyToMono(Map.class)
                                    .block();


                            boolean isSuccessful = responseBody.get("status").equals("CANCEL");
                            System.out.println(isSuccessful);



                            if (isSuccessful) {
                                System.out.println("is cancelled succesfully");
                            }
                            else{
                                System.out.println("is not cancelled succesfully");
                            }
                        } catch (Exception e) {

                        }

                    }



                }
            }

        }catch(Exception e) {
            // Handle any exceptions that might occur
        }




        return "";
    }

    public String moveBundle(String bundleId, String initCollection, String finalCollection,String email) throws ExecutionException, InterruptedException {
        System.out.println("Moving bundle now...");
        //var user = WebSecurityConfig.getUser();

        String result = "";

        DocumentReference sourceRef = db.collection("user").document(email).collection(initCollection).document(bundleId);
        DocumentReference destinationRef = db.collection("user").document(email).collection(finalCollection).document(bundleId);

        ApiFuture<DocumentSnapshot> future = sourceRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Map<String, Object> data = new HashMap<>(document.getData());
            destinationRef.set(document.getData());
            sourceRef.delete();
            result = ("Document " + bundleId + " moved from " + initCollection + " to " + finalCollection);
        } else {
            result = ("No document found with ID " + bundleId + " in collection " + initCollection);
        }

        System.out.println(result);
        return result;
    }


    public String moveBundle(String bundleId, String initCollection, String finalCollection, String email, Map<String, String> reservations) throws ExecutionException, InterruptedException {
        //var user = WebSecurityConfig.getUser();

        System.out.println("Moving bundle now...");

        String result = "";

        DocumentReference sourceRef = db.collection("user").document(email).collection(initCollection).document(bundleId);
        DocumentReference destinationRef = db.collection("user").document(email).collection(finalCollection).document(bundleId);

        ApiFuture<DocumentSnapshot> future = sourceRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Map<String, Object> data = new HashMap<>(document.getData());
            data.put("reservations", reservations);


            // Set the updated data to the destination collection
            destinationRef.set(data);
            sourceRef.delete();
            result = ("Document " + bundleId + " moved from " + initCollection + " to " + finalCollection);
        } else {
            result = ("No document found with ID " + bundleId + " in collection " + initCollection);
        }

        System.out.println(result);
        return result;
    }

    public ResponseEntity<String> buyBundle(Map<String, String> reservations, String bundleId, String email){
        System.out.println("i'm in buy");


        WebClient webClient = webClientBuilder.build();

        try {
//            DocumentReference bundleRef = db.collection("bundles").document(bundleId);
//            DocumentSnapshot bundleSnapshot = bundleRef.get().get();

            if (!reservations.isEmpty()) {
                //List<DocumentReference> productRefs = (List<DocumentReference>) bundleSnapshot.get("productIds");



                List<Thread> threads = new ArrayList<>();
                for (Map.Entry<String, String> entry : reservations.entrySet())  {
                    String reservationId = entry.getKey();
                    String url = entry.getValue();


                    String finalUrl = url + "reservations/" + reservationId + "/confirm";
                    System.out.println(finalUrl);

                    Thread thread = new Thread(() -> {
                        boolean confirmed = false;
//                        System.out.println("Waiting for confirmation...");
                        while (!confirmed) {
                            boolean isSuccessful=false;
                            if(url.equals("littleEndians")){

                                DocumentReference reservationRef = db.collection("reservations").document(reservationId);
                                DocumentSnapshot reservationDoc = null;
                                try {
                                    reservationDoc = reservationRef.get().get();
                                } catch (InterruptedException | ExecutionException e) {
                                    throw new RuntimeException(e);
                                }


                                if (reservationDoc.exists()) {
                                    // Update the reservation status to "CONFIRMED"
                                    reservationRef.update("status", "CONFIRMED");
                                    //isSuccessful = true;
                                } else {
                                    System.err.println("Reservation with ID " + reservationId + " not found.");
                                }

                                confirmed=true;

                                //isSuccessful=true;
                            }
                            else{
                                try {
                                    Map responseBody = webClient.post()
                                            .uri(finalUrl)
                                            .header("Authorization", "Bearer " + apiToken)
                                            .retrieve()
                                            .bodyToMono(Map.class)
                                            .block();


                                    isSuccessful = responseBody.get("status").equals("CONFIRMED");
                                    System.out.println(isSuccessful);



                                    if (isSuccessful) {
                                        System.out.println("is confirmed");
                                        confirmed=true;
                                    }
                                    else{
                                        System.out.println("is not confirmed");
                                    }
                                } catch (Exception e) {

                                }

                            }

                        }

                    });
                    threads.add(thread);
                }

                for (Thread thread : threads) {
                    System.out.println("Starting thread");
                    thread.start();

                }

                for (Thread thread : threads) {
                    try {
                        thread.join();
                        System.out.println("joined!");
                    } catch (InterruptedException e) {
                        System.out.println("uh oh D:");
                        String result = ("No document found with ID " + bundleId + " in collection " );
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to reserve.");
                    }
                }
                System.out.println("Ready for move");
                moveBundle(bundleId, "processing", "ordered", email);
                return ResponseEntity.ok("Bundle has been reserved");


            } else {
                // bruh it doesn't exist
                System.out.println("bruh it doens't exist");

            }




        } catch (Exception e) {
            // Handle any exceptions that might occur
        }








        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("you shound't have reached this");

    }


    @GetMapping("/api/getAllCustomers")
    @PreAuthorize("hasRole('manager')")
    public String getAllUsers() throws ExecutionException, InterruptedException {
        // Get a reference to the users collection
        System.out.println("in getAllUsers");
        CollectionReference usersRef = db.collection("user");

        // Get all user documents
        ApiFuture<QuerySnapshot> querySnapshot = usersRef.get();

        // Create an empty list to store user data
        List<Object> users = new ArrayList<>();

        // Loop through each user document
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            // Get user data (assuming your user documents have relevant fields)
            String id = document.getId();
            String email = document.getString("user");
            String role = document.getString("role");

            // Create a map to store user data (adjust fields based on your user document structure)
            Map<String, Object> user = new HashMap<>();
            user.put("email", email);
            user.put("role", role);

            // Add user data to the list
            users.add(user);
        }

        // Convert user data to JSON string using a JSON library (e.g., Gson)
        Gson gson = new Gson();
        String json = gson.toJson(users);

        System.out.println("json:"+json);

        return json;
    }


    @GetMapping("/api/getAllOrders")
    @PreAuthorize("hasRole('manager')")
    public String getAllOrders() throws ExecutionException, InterruptedException {

        System.out.println("yo");

        CollectionReference usersRef = db.collection("user");

        Map<String, List<Map<String, Object>>> allOrders = new HashMap<>();
        allOrders.put("processing", new ArrayList<>());
        allOrders.put("ordered", new ArrayList<>());

        ApiFuture<QuerySnapshot> querySnapshot = usersRef.get();

        for (QueryDocumentSnapshot userDoc : querySnapshot.get().getDocuments()) {
            String userId = userDoc.getId();

            // References to processing and ordered collections for the user
            CollectionReference processingRef = db.collection("user").document(userId).collection("processing");
            CollectionReference orderedRef = db.collection("user").document(userId).collection("ordered");

            // Add orders to the respective collections in the map
            allOrders.get("processing").addAll(getOrdersFromCollection(processingRef, userId));
            allOrders.get("ordered").addAll(getOrdersFromCollection(orderedRef, userId));
        }

        // Convert the map to JSON
        Gson gson = new Gson();
        String json = gson.toJson(allOrders);

        System.out.println(json);
        return json;
    }

    private List<Map<String, Object>> getOrdersFromCollection(CollectionReference collectionRef, String userId)
            throws ExecutionException, InterruptedException {
        List<Map<String, Object>> orders = new ArrayList<>();

        ApiFuture<QuerySnapshot> orderSnapshot = collectionRef.get();
        for (QueryDocumentSnapshot orderDoc : orderSnapshot.get().getDocuments()) {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("id", orderDoc.getString("id"));
            orderData.put("userId", userId);

            DocumentReference bundleRef = (DocumentReference) orderDoc.get("bundleRef");
            if (bundleRef != null) {
                DocumentSnapshot bundleSnapshot = bundleRef.get().get();
                if (bundleSnapshot.exists()) {
                    String bundleId = bundleSnapshot.getString("id");
                    orderData.put("bundleRef", bundleId);
                } else {
                    System.out.println("Bundle does not exist for order: " + orderDoc.getId());
                }
            } else {
                System.out.println("bundleRef is null for order: " + orderDoc.getId());
            }
            orders.add(orderData);
        }

        return orders;
    }


//    public String insertProducts() throws ExecutionException, InterruptedException {
//
//        Map<String, Object> product1 = new HashMap<>();
//        product1.put("description", "Look at this man go");
//        product1.put("imageLink", "https://as2.ftcdn.net/v2/jpg/01/18/39/53/1000_F_118395300_KEO4hFI9FASizysdfpHnPhNuNuNpqvA0.jpg");
//        product1.put("name", "mango");
//        product1.put("price", 500);
//        product1.put("supplier", "littleEndians");
//        product1.put("amount", 30);
//
//        // Insert the product into the 'products' collection
//        DocumentReference docRef = db.collection("products").document();
//        ApiFuture<WriteResult> result = docRef.set(product1);
//
//        Map<String, Object> product2 = new HashMap<>();
//        product2.put("description", "greatly beloved by the littleEndians");
//        product2.put("imageLink", "https://aliveplant.com/wp-content/uploads/2021/09/aphonso.jpeg");
//        product2.put("name", "alfonso mango");
//        product2.put("price", 40);
//        product2.put("supplier", "littleEndians");
//        product2.put("amount", 30);
//
//        // Insert the product into the 'products' collection
//        DocumentReference docRef2 = db.collection("products").document();
//        ApiFuture<WriteResult> result2 = docRef2.set(product2);
//
//        Map<String, Object> product3 = new HashMap<>();
//        product3.put("description", "clothing brand for men");
//        product3.put("imageLink", "https://s.yimg.com/uu/api/res/1.2/C3ISPlZxBRfb13CJXZ7lkg--~B/aD0xNjU0O3c9MjMzOTtzbT0xO2FwcGlkPXl0YWNoeW9u/http://media.zenfs.com/en_US/News/US-AFPRelax/mango_man.7bc5d111754.original.jpg");
//        product3.put("name", "mango man");
//        product3.put("price", 500);
//        product3.put("supplier", "littleEndians");
//        product3.put("amount", 30);
//
//        // Insert the product into the 'products' collection
//        DocumentReference docRef3 = db.collection("products").document();
//        ApiFuture<WriteResult> result3 = docRef3.set(product3);
//
//        Map<String, Object> product4 = new HashMap<>();
//        product4.put("description", "a delicious refreshment");
//        product4.put("imageLink", "https://goodtimein.co.uk/wp-content/uploads/2020/09/MANGOGO-250ml-Can-1080x1080px.jpg");
//        product4.put("name", "mango go");
//        product4.put("price", 800);
//        product4.put("supplier", "littleEndians");
//        product4.put("amount", 30);
//
//        // Insert the product into the 'products' collection
//        DocumentReference docRef4 = db.collection("products").document();
//        ApiFuture<WriteResult> result4 = docRef4.set(product4);
//
//        // Wait for the write to complete
//        System.out.println("Successfully written! " + result.get().getUpdateTime());
//
//        // Return a message or the document ID
//        return "Product inserted successfully";
//
//    }


}