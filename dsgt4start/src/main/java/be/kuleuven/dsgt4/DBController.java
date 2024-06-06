package be.kuleuven.dsgt4;

import be.kuleuven.dsgt4.auth.WebSecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.*;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.access.AuthorizationServiceException;
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
                String price = document.getString("price");
                String id = document.getString("id");

                // Append bundle details to the JSON string
                jsonDataBuilder.append("    {\n");
                jsonDataBuilder.append("      \"name\": \"").append(name).append("\",\n");
                jsonDataBuilder.append("      \"description\": \"").append(description).append("\",\n");
                jsonDataBuilder.append("      \"id\": \"").append(id).append("\",\n");
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

        // Reference to the user's document
        CollectionReference basketRef = db.collection("user").document(user.getEmail()).collection("basket");

        ApiFuture<QuerySnapshot> querySnapshot = basketRef.get();
        List<Map<String, Object>> shoppingCart = new ArrayList<>();

        StringBuilder jsonDataBuilder = new StringBuilder();
        jsonDataBuilder.append("{\n");
        jsonDataBuilder.append("  \"cart\": [\n");

        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {

            String id = document.getString("id");
            jsonDataBuilder.append("    {\n");
            jsonDataBuilder.append("    \"id\": \"").append(id).append("\",\n");

            DocumentReference bundleRef = (DocumentReference) document.get("bundleRef");
            String bundleName = "";
            String bundleId = "";


            DocumentSnapshot bundleSnapshot = bundleRef.get().get();
            if (bundleSnapshot.exists()) {
                // Extract product data from the product document
                bundleName = bundleSnapshot.getString("name");
                bundleId = bundleSnapshot.getString("id");
            } else {
                System.out.println("bundle does not exist");
            }

            jsonDataBuilder.append("    \"name\": \"").append(bundleName).append("\",\n");
            jsonDataBuilder.append("    \"bundleId\": \"").append(bundleId).append("\"\n");
            jsonDataBuilder.append("    },\n");

        }

        if (!querySnapshot.get().getDocuments().isEmpty()) {
            jsonDataBuilder.deleteCharAt(jsonDataBuilder.length() - 2); // Removes the last comma
        }

        jsonDataBuilder.append("]\n");
        jsonDataBuilder.append("}\n");

        //System.out.println(jsonDataBuilder.toString());
        return jsonDataBuilder.toString();
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


//    @PostMapping("/api/buyBundle")
//    public ResponseEntity<String> buyBundle(@RequestBody String bundleId) throws ExecutionException, InterruptedException {
//        // Print out the bundleId
//        System.out.println("Buying bundle with ID: " + bundleId);
//        // Return a success response
//        return ResponseEntity.status(HttpStatus.OK).body("Bundle with ID: " + bundleId);
//    }



    @GetMapping("/api/getProducts")
    public String getProducts() throws JsonProcessingException {
        WebClient webClient = webClientBuilder.build();

        StringBuilder jsonDataBuilder = new StringBuilder();
        jsonDataBuilder.append("{\n");
        jsonDataBuilder.append("  \"suppliers\": [\n");

        // Array of endpoint URLs
        String[] endpointURLs = {
                "http://sud.switzerlandnorth.cloudapp.azure.com:8080/products/",
                "http://ivan.canadacentral.cloudapp.azure.com:8080/products/",
                "http://sud.japaneast.cloudapp.azure.com:8080/products/"
        };


        // Loop through each endpoint
        for (String endpointURL : endpointURLs) {
            String responseBody = webClient.get()
                    .uri(endpointURL)
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

    private List<DocumentReference> addProduct(String[] productIds){
        //CollectionReference products = db.collection("products");
        WebClient webClient = webClientBuilder.build();
        int i=0;
        List<DocumentReference> documentReferences = new ArrayList<>();

        for (String id: productIds){

            String[] idParts = id.split("@");

            DocumentReference docRef = db.collection("products").document(idParts[1]);
            //System.out.println("TESTINGGG");

            ApiFuture<DocumentSnapshot> future = docRef.get();
            try {
                // Get the document snapshot
                DocumentSnapshot document = future.get();

                // Check if the document exists
                if (!document.exists()) {

                    String endpointURL=idParts[0]+idParts[1];
                    System.out.println(endpointURL);

                    String responseBody = webClient.get()
                            .uri(endpointURL)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();


                    try{

                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(responseBody);

                        Map<String, Object> data = new HashMap<>();
                        // Add data to the document as needed
                        data.put("name", rootNode.path("name").asText());
                        data.put("description", rootNode.path("description").asText());
                        data.put("imageLink", rootNode.path("imageLink").asText());
                        data.put("supplier", idParts[0].substring(0, idParts[0].length() - "products/".length()));

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
                documentReferences.add(docRef);

            } catch (InterruptedException | ExecutionException e) {
                // Handle any errors that may occur
                System.err.println("Error getting document: " + e.getMessage());
            }

            i++;
        }
        return documentReferences;
    }



    @PostMapping("/api/addBundle")
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

        List<DocumentReference> productIdFinal = addProduct(productIdSplit);

        // Create a map to hold the data for the new document
        Map<String, Object> data = new HashMap<>();
        data.put("id", "");
        data.put("name", bundleTitle);
        data.put("description", bundleDescription);
        data.put("productIds", productIdFinal);
        data.put("price", "$XX");

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
                    // Query all bundles except the current one to check for references to the product
                    Query query = db.collection("bundles").whereArrayContains("productIds", db.document("products/" + productId));
                    ApiFuture<QuerySnapshot> querySnapshot = query.get();

                    // If no other bundle except the current one references the product, delete it
                    if (querySnapshot.get().isEmpty()) {
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

    @PostMapping("/api/sendReservation")
    public String sendReservation(@RequestBody String bundleId) throws InterruptedException, ExecutionException {
        System.out.println("i am in reserve");
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

        DocumentReference orderRef = db.collection("user").document(user.getEmail()).collection("basket").document(bundleId);

        ApiFuture<DocumentSnapshot> future = orderRef.get();
        DocumentSnapshot orderSnap;
        try {
            orderSnap = future.get();
            //System.out.println(orderSnap.getData());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "Bundle Does not Exist!";
        }

        if (orderSnap.exists()) {

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

                String finalUrl = supplierUrl + "products/reserve";
                System.out.println(finalUrl);

                Map<String, Integer> productsToReserve = new HashMap<>();
                productsToReserve.put(productId, 1);
                try {
                    Map responseBody = webClient.post()
                            .uri(finalUrl)
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



                // Retrieve product data directly from Firestore
                //DocumentReference productRef = this.db.collection("products").document(productId);
//                DocumentSnapshot productSnapshot = productRef.get().get();
//                if (productSnapshot.exists()) {
//                    // Extract product data from the product document
//                    productId = productSnapshot.getId();
//                    String supplier = (String) productSnapshot.get("supplier");
//                    String endpointURL = "";
//                    for (String endpoint : endpointURLs)
//                        if (endpoint.contains(supplier)) {
//                            endpointURL = endpoint;
//                            break;
//                        }
//
//                    /** UNCOMMENT AFTER ENDPOINT FIXED **/
////                    String responseBody = webClient.get()
////                            .uri(endpointURL)
////                            .retrieve()
////                            .bodyToMono(String.class)
////                            .block();
////
////                    System.out.println(responseBody);
//
//                } else {
//                    System.out.println("product does not exist");
            }
//            }


            if (isSuccesful){
                System.out.println("Bundle reserved successfully");
                moveBundle(bundleId, "basket", "processing");
                buyBundle(reservations);
            }
            else{
                System.out.println("Bundle was not reserved successfully:(((((");

            }

        }


        // send reservation per bundle
        // [add supplier field to product later]
        // return bundle reference

        return "nice";

    }

    public String moveBundle(String bundleId, String initCollection, String finalCollection) throws ExecutionException, InterruptedException {
        var user = WebSecurityConfig.getUser();

        System.out.println("Moving bundle now...");

        String result = "";

        DocumentReference sourceRef = db.collection("user").document(user.getEmail()).collection(initCollection).document(bundleId);
        DocumentReference destinationRef = db.collection("user").document(user.getEmail()).collection(finalCollection).document(bundleId);

        ApiFuture<DocumentSnapshot> future = sourceRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            destinationRef.set(document.getData());
            sourceRef.delete();
            result = ("Document " + bundleId + " moved from " + initCollection + " to " + finalCollection);
        } else {
            result = ("No document found with ID " + bundleId + " in collection " + initCollection);
        }

        System.out.println(result);
        return result;
    }


    public String buyBundle(Map<String, String> reservations){


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

                        while (!confirmed) {
                            try {
                                String responseBody = webClient.post()
                                        .uri(finalUrl)
                                        .retrieve()
                                        .bodyToMono(String.class)
                                        .block();

                                System.out.println(responseBody);
                                confirmed=true;

                                // Check response for confirmation (modify this condition based on your supplier's response format)
//                                if (responseBody == "sth??") {//TODO
//                                    confirmed = true;
//                                }
                            } catch (Exception e) {

                            }
                        }

                    });
                    threads.add(thread);
                }

                for (Thread thread : threads) {
                    thread.start();
                }

                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        System.out.println("uh oh D:");
                    }

                }


            } else {
                // bruh it doesn't exist

            }




        } catch (Exception e) {
            // Handle any exceptions that might occur
        }





        return "";

    }


}