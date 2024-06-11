package be.kuleuven.dsgt4;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

@Service
public class StartupService {

    @Autowired
    RestTemplate restTemplate;

    private final DBController dbController;

    @Autowired
    public StartupService(DBController dbController) {
        this.dbController = dbController;
    }

    @PostConstruct
    public void checkPending() throws ExecutionException, InterruptedException {
        String orders = dbController.getAllOrders();

    }

    @PostConstruct
    public void checkStart() {
        System.out.println("We're post-constructing babyyyy");
    }

}
