package be.kuleuven.dsgt4;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java.util.Map;

@Service
public class StartupService {

    @Autowired
    RestTemplate restTemplate;

    private final DBController dbController;

    @Autowired
    Firestore db;

    @Autowired
    public StartupService(DBController dbController) {
        this.dbController = dbController;
    }

    @PostConstruct
    public void checkPending() throws ExecutionException, InterruptedException {
        String orders = dbController.getAllOrders();

        try {
            // Initialize ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            // Convert JSON string to Map
            Map<String, List<Map<String, Object>>> orderMap = objectMapper.readValue(orders, new TypeReference<Map<String, List<Map<String, Object>>>>(){});
            List<Map<String, Object>> processingMap = orderMap.get("processing");

            if (!(processingMap.isEmpty())){
                System.out.println("processing order processing");
                for (Map<String, Object> processedItem : processingMap){
                    String id = (String) processedItem.get("id");
                    String user = (String) processedItem.get("userId");

                    DocumentReference orderRef = db.collection("user").document(user).collection("processing").document(id);
                    DocumentSnapshot orderSnap = orderRef.get().get();
                    Map<String, String> reservations = (Map<String, String>) orderSnap.get("reservations");

                    dbController.buyBundle(reservations,id, user);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
