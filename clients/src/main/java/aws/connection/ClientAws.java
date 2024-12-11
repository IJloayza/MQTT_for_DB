package aws.connection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPrivateKeySpec;
import java.sql.SQLException;
import java.util.Base64;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

public class ClientAws{
    private static final String FilePrivKey = "/home/isard/MQTT_for_DB/clients/src/main/java/aws/certificates/file-private.pem.key";
    private static final String FileCertIot = "/home/isard/MQTT_for_DB/clients/src/main/java/aws/certificates/file-certificate.pem.crt";
    private static final String EndPoint = "a3hq611w41tavz-ats.iot.us-east-1.amazonaws.com";
    private static final String clientId = "manolete";
    private static final String Topic = "node01/aws-arduino";
    private static String alias = "awsKey";
    private static String pw = "pa44word";
    private static final AWSIotQos QOS = AWSIotQos.QOS0;

    private static AWSIotMqttClient client;

    public static void setClient(AWSIotMqttClient client){
        ClientAws.client = client;
    }

    public static KeyStore createKeyStore(String alias, String certificateFile, String privateKeyFile, String password) throws Exception {
        // Cargar el certificado X.509
         StringBuilder base64Content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(certificateFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("-----")) { // Ignorar cabeceras y pies
                        base64Content.append(line);
                    }
                }
            }
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content.toString());

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate;
        try (InputStream certStream = new ByteArrayInputStream(decodedBytes)) {
            certificate = (X509Certificate) certFactory.generateCertificate(certStream);
        }
        
        // Cargar la clave privada
        String key = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(privateKeyFile)), "UTF-8");

        // Eliminar cabeceras y pies
        key = key.replace("-----BEGIN RSA PRIVATE KEY-----", "")
                 .replace("-----END RSA PRIVATE KEY-----", "")
                 .replaceAll("\\s+", ""); // Eliminar espacios y saltos de línea

        // Decodificar Base64
        byte[] keyBytes = Base64.getDecoder().decode(key);

        // Parsear clave PKCS#1
        ASN1Primitive asn1 = ASN1Primitive.fromByteArray(keyBytes);
        RSAPrivateKey rsaPrivateKey = RSAPrivateKey.getInstance((ASN1Sequence) asn1);

        // Convertir a una clave privada compatible con Java
        RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPrivateExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        // Crear un KeyStore y cargar la clave privada y el certificado
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, password.toCharArray());
        keyStore.setKeyEntry(alias, keyFactory.generatePrivate(spec), password.toCharArray(), new Certificate[]{certificate});

        return keyStore;
    }

    public static void startClient(){
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

    public static void publishAws(boolean isvalid, String uid){
        try {
            if(isvalid){
                String valid = "1";
                String payload = "{\"answer\": \"" + valid + "\"}";
                client.publish(Topic, payload);
            }else if(!isvalid){
                String valid = "0";
                String payload = "{\"answer\": \"" + valid + "\"}";
                AWSIotMessage message = new NonBlockingPublishListener(Topic, QOS, payload);
                client.publish(message);
            }
            
            
        } catch (AWSIotException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Error trying to publish in topic " + Topic);
        }
    }

    public static void main(String[] args) throws SQLException {
        ClientAws.connectAws();
        ClientAws.subscribeAws();
    }
}
