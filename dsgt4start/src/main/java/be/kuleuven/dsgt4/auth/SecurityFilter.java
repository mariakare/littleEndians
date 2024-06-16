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
            response.sendRedirect("/");
            return;
        }

        //token="eyJhbGciOiJSUzI1NiIsImtpZCI6ImRmOGIxNTFiY2Q5MGQ1YjMwMjBlNTNhMzYyZTRiMzA3NTYzMzdhNjEiLCJ0eXAiOiJKV1QifQ.eyJyb2xlcyI6Im1hbmFnZXIiLCJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vZHNndC1saXR0bGUtZW5kaWFuIiwiYXVkIjoiZHNndC1saXR0bGUtZW5kaWFuIiwiYXV0aF90aW1lIjoxNzE4NTY4MTQwLCJ1c2VyX2lkIjoiOWNWMTBtZnJoRlVXYzRuNGFpak53VDNuUlFnMSIsInN1YiI6IjljVjEwbWZyaEZVV2M0bjRhaWpOd1QzblJRZzEiLCJpYXQiOjE3MTg1NjgxNDAsImV4cCI6MTcxODU3MTc0MCwiZW1haWwiOiJtYW5hZ2VyQG1hbmFnZXIuY29tIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7ImVtYWlsIjpbIm1hbmFnZXJAbWFuYWdlci5jb20iXX0sInNpZ25faW5fcHJvdmlkZXIiOiJwYXNzd29yZCJ9fQ.MbGc-SWi0SmaDFG2h1lHeAaQJwdXk137-OnXXPwTJO397ooT1VxTnv4G8UfYXcyUq4MUWNMZcaHdl35Txv_t6BVg1gc_1N9ZezzH6ZVPH6JZHvrWiVoAA-e_pb6f9QPI-G-0gkmsg8HSVpEtdLum75r208cO3Y6e4kCPqtgu0egwXFIUHt3r-EXfvUnMoClSa47zE1GLiY1LzSixN64dr-HYLnAot5MAv47aiZKDO2kVZIzOcmMIBy_YLAbn4WIF7nxTLyKPDKRDfGTQcX_i9QQLxd73DPPc8CfUL0sSLSg5LR7GO_doq8jTVDxj6rAAZVgdWpcdxXiPGZ_1SoZNBw";        // Fetch public keys from Google
        Map<String, String> publicKeys = null;
        try {
            publicKeys = fetchPublicKeys();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Verify the token
        boolean isValid = verifyToken(token, publicKeys);

        if (isValid) {
            System.out.println("Token is valid.");
        } else {
            System.out.println("Token is invalid.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid token.");
            response.sendRedirect("/");
            return; // Terminate the filter chain here
        }

        DecodedJWT decodedJWT = JWT.decode(token);
        String email = decodedJWT.getClaim("email").asString();
        String role = decodedJWT.getClaim("roles").asString();

        if (role == null || role.isEmpty()) {
            role = "user"; //we see what to do w this later
            System.out.println("the role was null");
        }

        System.out.println("My current role is: " + role);

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
            String kid = jwt.getKeyId();
            String publicKey = publicKeys.get(kid);

            // Convert the public key string to an RSAPublicKey object
            System.out.println("the public key we try to decode is:");
            publicKey = publicKey//.replaceAll("\\n", "")
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s", "");
            System.out.println(publicKey);

            byte[] decodedBytes = Base64.getDecoder().decode(publicKey);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(decodedBytes));

            PublicKey pk = certificate.getPublicKey();

            if (pk instanceof RSAPublicKey) {
                RSAPublicKey rsaPublicKey = (RSAPublicKey) pk;

                Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, null);

                JWTVerifier verifier = JWT.require(algorithm)
                        .withIssuer("https://securetoken.google.com/dsgt-little-endian")
                        .build();
                verifier.verify(token);
                return true;

            } else {
                System.out.println("The public key is not an instance of RSAPublicKey");
            }

            return false;

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