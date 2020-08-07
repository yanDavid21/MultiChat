package client.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.TrustManagerFactory;

public class MultiChatClientModelImpl implements MultiChatModel {

  private final String ipAddress;
  private Scanner in;
  private PrintWriter out;
  private SSLSocket socket;
  private String name;

  public MultiChatClientModelImpl(String ipAddress, int portNumber) throws IOException {
    this.ipAddress = ipAddress;

    try {
      socket = initSSLDetailsAndGetClientSocket(ipAddress, portNumber);
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }

    handleHandshake();
    wrapClientIO();
    checkSocketError();
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
    return (SSLSocket)factory.createSocket(ipAddress, portNumber);
  }

  //starts the handshake process with the connected server socket and adds a listener for a
  //successful connection
  private void handleHandshake() throws IOException {
    socket.addHandshakeCompletedListener(e -> printSuccessfulHandshake(e));
    socket.startHandshake();
  }

  //prints to the console if the handshake was successful, along with session information
  private void printSuccessfulHandshake(HandshakeCompletedEvent e) {
    System.out.println("Handshake successful: " + e.getSession());
  }

  //wraps the input and output streams in a Scanner and PrintWriter respectively
  private void wrapClientIO() throws IOException {
    in = new Scanner(socket.getInputStream());
    out = new PrintWriter(socket.getOutputStream(), true);
  }

  //prints an error message if the output stream of the socket has an issue
  private void checkSocketError() {
    if (out.checkError())
      System.out.println(
          "SSLSocketClient:  java.io.PrintWriter error");
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
}
