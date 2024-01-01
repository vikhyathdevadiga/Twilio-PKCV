import com.twilio.http.TwilioRestClient;
import com.twilio.http.ValidationClient;
import com.twilio.rest.api.v2010.account.Message;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class CustomPKCVClient {
    static PrivateKey privateKey;
    public static final String PRIVATE_KEY_FILE = System.getenv("PRIVATE_KEY_FILE_NAME");
    public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String API_KEY_SID = System.getenv("TWILIO_API_KEY_SID");
    public static final String API_KEY_SECRET = System.getenv("TWILIO_API_KEY_SECRET");
    public static final String PUBLIC_KEY_SID = System.getenv("TWILIO_PUBLIC_KEY_SID");


    public static void main(String[] args) throws Exception {

        privateKey = getPrivateKeyFromFile(PRIVATE_KEY_FILE);

        TwilioRestClient validationClient = new TwilioRestClient.Builder(API_KEY_SID, API_KEY_SECRET)
                .accountSid(ACCOUNT_SID)
                // Validation client supports RS256 or PS256 algorithm. Default is RS256.
                .httpClient(new ValidationClient(ACCOUNT_SID, PUBLIC_KEY_SID, API_KEY_SID, privateKey))
                .build();

        Iterable<Message> messages = Message.reader().limit(2).read(validationClient);
        for (Message message : messages) {
            System.out.println(message.getBody());
        }
    }

    public static PrivateKey getPrivateKeyFromFile(String privateKeyFilePath) throws Exception {
        // Read the content of the private key file
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get(privateKeyFilePath));

        // Remove the first and last lines if the private key file contains headers and footers
        String privateKeyPEM = new String(privateKeyBytes);
        if (privateKeyPEM.contains("-----BEGIN PRIVATE KEY-----") && privateKeyPEM.contains("-----END PRIVATE KEY-----")) {
            privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "");
            privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
            privateKeyPEM = privateKeyPEM.replaceAll("\\s", "");
        }

        // Decode the Base64 encoded bytes
        byte[] decodedKeyBytes = Base64.getDecoder().decode(privateKeyPEM);

        // Create a PKCS8EncodedKeySpec from the decoded bytes
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKeyBytes);

        // Get an instance of the key factory and generate the private key
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}