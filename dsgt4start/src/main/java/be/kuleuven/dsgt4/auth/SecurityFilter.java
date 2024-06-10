package be.kuleuven.dsgt4.auth;

import be.kuleuven.dsgt4.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import com.auth0.jwt.JWTVerifier;




@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final String GOOGLE_PUB_KEY_ENDPOINT = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"; // Public key endpoint
    private static final String JWT_ALGORITHM = "RS256"; // Algorithm used for signing



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // TODO: (level 1) decode Identity Token and assign correct email and role

        String authorizationHeader = request.getHeader("Authorization");
        String token= null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing Authorization header.");
            return;
        }

        token= "eyJhbGciOiJSUzI1NiIsImtpZCI6ImRmOGIxNTFiY2Q5MGQ1YjMwMjBlNTNhMzYyZTRiMzA3NTYzMzdhNjEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZHNndC1saXR0bGUtZW5kaWFuIiwiYXVkIjoiZHNndC1saXR0bGUtZW5kaWFuIiwiYXV0aF90aW1lIjoxNzE4MDU4OTkyLCJ1c2VyX2lkIjoiOWNWMTBtZnJoRlVXYzRuNGFpak53VDNuUlFnMSIsInN1YiI6IjljVjEwbWZyaEZVV2M0bjRhaWpOd1QzblJRZzEiLCJpYXQiOjE3MTgwNTg5OTIsImV4cCI6MTcxODA2MjU5MiwiZW1haWwiOiJtYW5hZ2VyQG1hbmFnZXIuY29tIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7ImVtYWlsIjpbIm1hbmFnZXJAbWFuYWdlci5jb20iXX0sInNpZ25faW5fcHJvdmlkZXIiOiJwYXNzd29yZCJ9fQ.cDyIwjFSiK4QCTJkxmchM8d5voAv0Lx066VRM21Oz4eHhvRcFCfCx9RjhWZAmNUseRPurdAfp9hcPowKZpyiLqO9WXFai64OPEZQxUG71om7h-01KRUjFqJy6xNKDlRaX1YokqTVkN03a7sPHEzC1liAeV6CijfGiF20mHG3mgqYKmXwkpTIsuq5HD4J9LpjAhkpjEHq9zb4ITR8G166SvfuYUIvAfPwmwFeySZgO-C8od27tbHStSdmVDMEofnSncYTmFX9T4dBBJKLB9aFMb3Oa3coyJDhVkXBcwWlniarHCn0H02i5s_AdV__RRsHUnigUFq1TcZUlzW_NpfH-w";



        // Fetch public keys from Google
        Map<String, String> publicKeys = null;
        try {
            publicKeys = fetchPublicKeys();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Verify the token
        DecodedJWT decodedJWT = JWT.decode(token);
        String header = decodedJWT.getHeader();
        byte[] signatureBytes = Base64.getUrlDecoder().decode(header);

        // Convert bytes to string
        String signatureValue = new String(signatureBytes);

        // Print the signature
        System.out.println("Signature: " + signatureValue);
        // Print the signature
//        System.out.println("Signature: " + signature);


        boolean isValid = verifyToken(token, publicKeys);
//        System.out.println("Is valid: "+ String.valueOf(isValid));

        if (isValid) {
            System.out.println("Token is valid.");
        } else {
            System.out.println("Token is invalid.");
            return;
        }


//        // 1. Fetch the public key
//        String publicKey;
//
//        try {
//            publicKey = fetchPublicKeys();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//
//        // 2. Verify the JWT signature
//        JWTVerifier verifier = JWT.require(Algorithm.RSA256(getRSAPublicKey(publicKey), null))
//                .withIssuer("https://securetoken.google.com/") // Set issuer based on your Firebase project
//                .build();
//
//
//
//
//
//
//        DecodedJWT decodedJWT = verifier.verify(token);//JWT.decode(token);
////        System.out.println("Decoded JWT: " + decodedJWT);
//        logger.info("Okay so here we go. We start with the toString:");
//        logger.info(decodedJWT.toString());
//        logger.info("issuer:");
//        logger.info(decodedJWT.getIssuer());
//        logger.info("email:");
//        logger.info(decodedJWT.getClaim("email").asString());

//        DecodedJWT decodedJWT = JWT.decode(token);
        String email = decodedJWT.getClaim("email").asString();
        //System.out.println("Email: " + email);
        String role = decodedJWT.getClaim("role").asString();
        if (role == null || role.isEmpty()) {
            role = "user"; //we see what to do w this later
        }
        var user = new User(email, role);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new FirebaseAuthentication(user));

        filterChain.doFilter(request, response);
    }

    private static Map<String, String> fetchPublicKeys() throws Exception {
        URL url = new URL(GOOGLE_PUB_KEY_ENDPOINT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse the JSON response
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response.toString()).getAsJsonObject();

        // Extract public keys
        Map<String, String> publicKeys = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            publicKeys.put(entry.getKey(), entry.getValue().getAsString());
        }

        return publicKeys;
    }

    private static boolean verifyToken(String token, Map<String, String> publicKeys) {
        try {
            // Decode the token
            DecodedJWT jwt = JWT.decode(token);

            // Get the kid (key ID) from the token header
            String kid = jwt.getKeyId();

            // Get the corresponding public key from the map
            String publicKey = publicKeys.get(kid);


// Convert the public key string to an RSAPublicKey object
            System.out.println("the public key we try to decode is:");
            publicKey = publicKey//.replaceAll("\\n", "")
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
//                    .replaceAll("\\n", "");
                    .replaceAll("\\s", "");
            System.out.println(publicKey);





//            String pem = new String(Base64.getDecoder().decode(publicKey)); // default cs okay for PEM
//            String[] lines = pem.split("\n");
////            lines[0] = lines[lines.length-1] = "";
//            String body = String.join("", lines);
//
//            System.out.println("decoded cert:");
//            System.out.println(body);
            // in general split on "\r?\n" (or delete "\r" and split on "\n")
            //or instead:
            //String body = pem.replaceAll("-----(BEGIN|END) RSA PUBLIC KEY-----\n","").replaceAll("\n", ""); // or "\r?\n"

//            KeyFactory kf = KeyFactory.getInstance("RSA");
//            PublicKey key = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
//            // for test only:
//            System.out.println ( ((java.security.interfaces.RSAPublicKey)key).getModulus() );

            byte[] decodedBytes = Base64.getDecoder().decode(publicKey);
            // Create a CertificateFactory
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            // Create an X509Certificate object
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(decodedBytes));

            // Extract the public key from the certificate
            PublicKey pk = certificate.getPublicKey();

// Check if the public key is an instance of RSAPublicKey
            if (pk instanceof RSAPublicKey) {
                RSAPublicKey rsaPublicKey = (RSAPublicKey) pk;

                // Print the RSA public key (optional)
                System.out.println("RSA Public Key: " + rsaPublicKey);
                System.out.println("Modulus: " + rsaPublicKey.getModulus());
                System.out.println("Exponent: " + rsaPublicKey.getPublicExponent());


                Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, null);


                JWTVerifier verifier = JWT.require(algorithm)
                        .withIssuer("https://securetoken.google.com/dsgt-little-endian")
//                        .acceptLeeway(60*60*3)// Replace with your project ID
                        .build();
                verifier.verify(token);
                return true;

            } else {
                System.out.println("The public key is not an instance of RSAPublicKey");
            }

            //String encodedCertificate = publicKey.split("\n")[1]; // Extract the certificate content
//            byte[] decodedCertificate = Base64.getDecoder().decode(publicKey);
//            RSAPublicKey pk = (RSAPublicKey)publicKey;


            //                System.out.println(decodedCertificate);
//                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
//                X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(decodedCertificate));
//                pk = (RSAPublicKey) certificate.getPublicKey();


//                KeyFactory kf = KeyFactory.getInstance("RSA");
//                RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(
//                        new BigInteger(1, decodedCertificate),
//                        BigInteger.valueOf(65537)); // Exponent value for RSA public keys
//                RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(rsaPublicKeySpec);


//                  X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(decodedCertificate);
//                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//                    RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);


            return false;

            //            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
//            String token = // Your JWT token;
//                    JWTVerifier verifier = JWT.require(algorithm)
//                    .withIssuer("securetoken.google.com/dsgt-little-endian")
//                    .build()
//                    .verify(token);

// Access claims from the verified JWT using verifier.getClaims()
//            byte[] decodedKey = Base64.getDecoder().decode(publicKey);
//            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(decodedKey);
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
//            return false;
//            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
//            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);


//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
////            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(spec);
//            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getMimeDecoder().decode(publicKey));
//            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);

// Verify the signature using the RSAPublicKey


//            return false;
        } catch (JWTVerificationException e) {
            System.err.println("Token verification failed: " + e.getMessage());
            return false;
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith("/api");
    }

    private static class FirebaseAuthentication implements Authentication {
        private final User user;

        FirebaseAuthentication(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            if (user.isManager()) {
                return List.of(new SimpleGrantedAuthority("manager"));
            } else {
                return new ArrayList<>();
            }
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public User getPrincipal() {
            return this.user;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public void setAuthenticated(boolean b) throws IllegalArgumentException {

        }

        @Override
        public String getName() {
            return null;
        }


    }
}




//public class TokenVerifier {
//
//    private static final String GOOGLE_CERT_URL = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com";
//
//    public static void main(String[] args) throws Exception {
//        // Replace with your actual token
//        String token = "YOUR_FIREBASE_ID_TOKEN";
//
//        // Fetch public keys from Google
//        Map<String, String> publicKeys = fetchPublicKeys();
//
//        // Verify the token
//        boolean isValid = verifyToken(token, publicKeys);
//
//        if (isValid) {
//            System.out.println("Token is valid.");
//        } else {
//            System.out.println("Token is invalid.");
//        }
//    }
//
//    private static Map<String, String> fetchPublicKeys() throws Exception {
//        URL url = new URL(GOOGLE_CERT_URL);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            response.append(line);
//        }
//        reader.close();
//
//        // Parse the JSON response
//        JsonParser parser = new JsonParser();
//        JsonObject jsonObject = parser.parse(response.toString()).getAsJsonObject();
//
//        // Extract public keys
//        Map<String, String> publicKeys = new HashMap<>();
//        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
//            publicKeys.put(entry.getKey(), entry.getValue().getAsString());
//        }
//
//        return publicKeys;
//    }
//
//    private static boolean verifyToken(String token, Map<String, String> publicKeys) {
//        try {
//            // Decode the token
//            DecodedJWT jwt = JWT.decode(token);
//
//            // Get the kid (key ID) from the token header
//            String kid = jwt.getKeyId();
//
//            // Get the corresponding public key from the map
//            String publicKey = publicKeys.get(kid);
//
//            // Verify the signature using the public key
//            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
//            JWTVerifier verifier = JWT.require(algorithm)
//                    .withIssuer("https://securetoken.google.com/YOUR_PROJECT_ID") // Replace with your project ID
//                    .build();
//            verifier.verify(token);
//
//            return true;
//        } catch (JWTVerificationException e) {
//            System.err.println("Token verification failed: " + e.getMessage());
//            return false;
//        }
//    }
//}

