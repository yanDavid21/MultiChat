package client.model;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Scanner;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Represents a standard model implementation of the MultiChatClient, handling the internal processes of the client.
 * Stores the IP address to the desired server, the SSLSocket to the server, a Scanner and PrintWriter object wrapped
 * around the SSL (Secure Socket Layer) Socket, and the username of the client.
 */
public class MultiChatClientModelImpl implements MultiChatModel {

    private final String ipAddress;
    private Scanner in;
    private PrintWriter out;
    private final SSLSocket socket;
    private String name;

    /**
     * Creates a instance of this model and creates a SSLSocket to the given IP address and port number.
     *
     * @param ipAddress  the IP address of the server
     * @param portNumber the port number of the server
     * @throws IOException when the server cannot be reached or successfully connected to
     */
    public MultiChatClientModelImpl(String ipAddress, int portNumber) throws IOException {
        this.ipAddress = ipAddress;
        try {
            socket = initSSLDetailsAndGetClientSocket(ipAddress, portNumber);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }

        socket.startHandshake();
        wrapClientIO();
    }

    //returns the SSLSocket initialized with the keys and algorithm to use for encryption
    private SSLSocket initSSLDetailsAndGetClientSocket(String ipAddress, int portNumber)
            throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException,
            UnrecoverableKeyException, KeyManagementException {

        SSLSocketFactory factory;
        SSLContext ctx;
        KeyManagerFactory kmf;
        KeyStore ks;
        char[] passphrase = "socketpractice".toCharArray(); //password used for keystore

        //specifies TLS protocol, SunX509 key manager algorithm, and .jks keystore file types to be used
        ctx = SSLContext.getInstance("TLS");
        kmf = KeyManagerFactory.getInstance("SunX509");
        ks = KeyStore.getInstance("JKS");

        //loads the keystore to be used for encryption
        ks.load(this.getClass().getClassLoader().getResourceAsStream(
                "client/resources/keystore/server_keystore.jks"), passphrase);
        kmf.init(ks, passphrase);

        //initializes the trust store of the client (what the client can trust, aka. certificates)
        InputStream myKeys = this.getClass().getClassLoader().getResourceAsStream(
                "client/resources/keystore/clientTrustStore.jts");
        KeyStore myTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        myTrustStore.load(myKeys, "socketpractice".toCharArray());
        myKeys.close();
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(myTrustStore);

        //initializes the SSL context to the key and trust stores with a default security provider
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        //return a SSL socket with all the specific SSL context
        factory = ctx.getSocketFactory();
        return (SSLSocket) factory.createSocket(ipAddress, portNumber);
    }

    //wraps the input and output streams in a Scanner and PrintWriter respectively
    private void wrapClientIO() throws IOException {
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public boolean isConnectionRunning() {
        return in.hasNextLine();
    }

    @Override
    public String getSocketInput() {
        return in.nextLine();
    }

    @Override
    public void sendText(String output) {
        out.println(output);
    }

    @Override
    public MultiChatClientModelImpl switchPorts(String portNumber) throws IOException,
            NumberFormatException {
        return new MultiChatClientModelImpl(this.ipAddress, Integer.parseInt(portNumber));
    }

    @Override
    public void setUsername(String username) {
        name = username;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public void sendFile(String fileName, long filesize, File file) throws IOException {
        out.println("/file " + fileName + ":" + filesize);
        sendFileDownStream(file);
    }

    @Override
    public void sendPrivateFile(String fileName, long fileSize, File file, String receiver, String sender)
            throws IOException {
        out.println("/privatefile " + receiver + ":" + fileName + ":" + fileSize);
        sendFileDownStream(file);
    }

    //reads a file and sends it down the socket's output stream
    private void sendFileDownStream(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] buffer = new byte[4096];
        int amountRead;
        while ((amountRead = bis.read(buffer, 0, buffer.length)) > -1) {
            socket.getOutputStream().write(buffer, 0, amountRead);
        }
        socket.getOutputStream().flush();
        fis.close();
        bis.close();
    }

    @Override
    public void saveFile(File file, long fileSize) throws IOException {
        if (file != null) {
            byte[] buf = new byte[4096];
            FileOutputStream fos = new FileOutputStream(file);
            //read file
            while (fileSize > 0 && socket.getInputStream().read(
                    buf, 0, (int) Math.min(buf.length, fileSize)) > -1) {
                fos.write(buf, 0, buf.length);
                fileSize -= buf.length;
            }
            fos.flush();
            fos.close();
        } else {
            socket.getInputStream().skip(fileSize);
        }
    }

}

