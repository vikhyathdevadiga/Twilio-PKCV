import com.twilio.http.TwilioRestClient;
import com.twilio.http.ValidationClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.NewSigningKey;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CustomPKCVClient1 {
    static PrivateKey privateKey;
    static PublicKey publicKey;

    public static final String PUBLIC_KEY_FILE = "public_key.dat";
    public static final String SIGNING_KEY_FILE = "signing_key.dat";

    public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");

    public static final String RSA_PUBLIC_KEY_FILE = "public.key";
    public static final String RSA_PRIVATE_KEY_FILE = "private.key";

    public static void main(String[] args) throws Exception {

        // Create a File object with the specified path
        File publicKeyFile = new File(RSA_PUBLIC_KEY_FILE);

        if (publicKeyFile.exists()) {
            publicKey = getPublicKeyFromFile(RSA_PUBLIC_KEY_FILE);
            privateKey = getPrivateKeyFromFile(RSA_PRIVATE_KEY_FILE);
        } else {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
            try (FileOutputStream fos = new FileOutputStream(RSA_PUBLIC_KEY_FILE)) {
                fos.write(publicKey.getEncoded());
            }
            try (FileOutputStream fos = new FileOutputStream(RSA_PRIVATE_KEY_FILE)) {
                fos.write(privateKey.getEncoded());
            }
        }

        TwilioRestClient client =
                new TwilioRestClient.Builder(ACCOUNT_SID, AUTH_TOKEN)
                        .build();

        com.twilio.rest.accounts.v1.credential.PublicKey key = createOrLoadPublicKey(client);
        NewSigningKey signingKey = createOrLoadSigningKey(client);

        TwilioRestClient validationClient = new TwilioRestClient.Builder(signingKey.getSid(), signingKey.getSecret())
                .accountSid(ACCOUNT_SID)
                // Validation client supports RS256 or PS256 algorithm. Default is RS256.
                .httpClient(new ValidationClient(ACCOUNT_SID, key.getSid(), signingKey.getSid(), privateKey))
                .build();

        Iterable<Message> messages = Message.reader().limit(2).read(validationClient);
        for (Message message : messages) {
            System.out.println(message.getBody());
        }
    }

    public static PublicKey getPublicKeyFromFile(String fileName) throws Exception {
        File publicKeyFile = new File(fileName);
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(publicKeySpec);
    }

    public static PrivateKey getPrivateKeyFromFile(String fileName) throws Exception {
        File privateKeyFile = new File(fileName);
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        return keyFactory.generatePrivate(privateKeySpec);
    }

    private static com.twilio.rest.accounts.v1.credential.PublicKey createOrLoadPublicKey(TwilioRestClient client) throws IOException, ClassNotFoundException {
        File publicKeyFile = new File(PUBLIC_KEY_FILE);

        if (publicKeyFile.exists()) {
            // Load the existing PublicKey from the file
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(publicKeyFile))) {
                return (com.twilio.rest.accounts.v1.credential.PublicKey) ois.readObject();
            }
        } else {
            // Create a new PublicKey and save it to the file
            com.twilio.rest.accounts.v1.credential.PublicKey key = com.twilio.rest.accounts.v1.credential.PublicKey.creator(
                    Base64.getEncoder().encodeToString(publicKey.getEncoded())
            ).setFriendlyName("Public Key").create(client);
            saveObjectToFile(key, publicKeyFile);
            return key;
        }
    }


    private static NewSigningKey createOrLoadSigningKey(TwilioRestClient client) throws IOException, ClassNotFoundException {

        File signingKeyFile = new File(SIGNING_KEY_FILE);

        if (signingKeyFile.exists()) {
            // Load the existing NewSigningKey from the file
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(signingKeyFile))) {
                return (NewSigningKey) ois.readObject();
            }
        } else {
            // Create a new NewSigningKey and save it to the file
            NewSigningKey signingKey = NewSigningKey.creator().create(client);
            saveObjectToFile(signingKey, signingKeyFile);
            return signingKey;
        }
    }

    private static void saveObjectToFile(Serializable object, File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(object);
        }
    }
}