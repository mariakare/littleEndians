package be.kuleuven.dsgt4;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Service
public class StartupService {

    @Autowired
    RestTemplate restTemplate;

    @PostConstruct
    public void checkPending() {
        String baseUrl = "http://localhost:8080"; // Base URL of your application
        String function1Url = baseUrl + "/function1";

        String response1 = restTemplate.getForObject(function1Url, String.class);

    }

}
