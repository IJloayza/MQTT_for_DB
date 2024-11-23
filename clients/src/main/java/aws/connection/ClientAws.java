package aws.connection;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;

public class ClientAws{
    private static final String FilePrivKey = "";
    private static final String FileCertIot = "";
    private static final String EndPoint = "";
    private static final String clientId = "";
    private static final String Topic = "levels/Topic/toSub";
    private static String alias = "aliasForKeyEntry";
    private static String pw = "passwordForKey";
    private static final AWSIotQos QOS = AWSIotQos.QOS0;

    private static AWSIotMqttClient client;

    public static void setClient(AWSIotMqttClient client){
        ClientAws.client = client;
    }

    public static KeyStore createKeyStore(String alias, String certificateFile, String privateKeyFile, String password) throws Exception {
        // Cargar el certificado X.509
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate;
        try (FileInputStream certStream = new FileInputStream(certificateFile)) {
            certificate = (X509Certificate) certFactory.generateCertificate(certStream);
        }

        // Cargar la clave privada
        String privateKeyContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(privateKeyFile)))
                .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);

        // Crear una clave privada usando PKCS8
        java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes);
        java.security.PrivateKey privateKey = java.security.KeyFactory.getInstance("RSA").generatePrivate(keySpec);

        // Crear un KeyStore y cargar la clave privada y el certificado
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, password.toCharArray());
        keyStore.setKeyEntry(alias, privateKey, password.toCharArray(), new Certificate[]{certificate});

        return keyStore;
    }

    public static void startClient(){
        if(client == null){
            throw new IllegalArgumentException("The client must exists, try setClient to create one");
        }
        try {
            KeyStore keys = createKeyStore(alias, FileCertIot, FilePrivKey, pw);
            if (keys != null) {
                client = new AWSIotMqttClient(EndPoint, clientId, keys, pw);
                if(client == null){
                    throw new Exception("Error in client creation, it has not been created");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static void connectAws(){
        try {
            startClient();
            client.connect();
        } catch (AWSIotException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Error connecting to AWS");
        }
    }

    public static void subscribeAws(){
        try {
            Topic topic = new Topic(Topic, QOS);
            client.subscribe(topic);
        } catch (AWSIotException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Error trying subscription in topic " + Topic);
        }
    }

    public static void main(String[] args) {
        ClientAws.connectAws();
        ClientAws.subscribeAws();
    }
}
