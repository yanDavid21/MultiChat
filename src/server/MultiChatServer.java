package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * Represents a simple-text server handling the input and output to several clients (30).
 */
public class MultiChatServer {

  //holds the names of active clients
  private static HashSet<String> names = new HashSet<>();

  //a set of writers that write to the output of a client's socket
  private static HashSet<PrintWriter> outputWriters = new HashSet<>();

  //a set of the current active servers
  private static HashSet<String> serverNames = new HashSet();

  /**
   * Main method to start the MultiChat server and listens for 1<=x<=args connections on local port
   * 59090 where args is the desired number of possible clients (default 30). Creating a thread for
   * each connection and creating and running a Task for each connection.
   *
   * @param args command line arguments describing the desired number of clients
   * @throws IOException when a creation of ServerSocket fails and/or the listening for
   * a connection to the ServerSocket has failed.
   * @throws IllegalArgumentException when supplied more than one argument
   * @throws NumberFormatException when given a non-integer argument.
   */
  public static void main(String[] args) throws IllegalArgumentException,
      NumberFormatException {

    //handle the command line argument for amount of clients on a server (default 30)
    int possibleAmountOfClients;
    if (args.length == 2) {
      possibleAmountOfClients = Integer.parseInt(args[1]);
    } else if (args.length == 1) {
      possibleAmountOfClients = 30;
    } else {
      throw new IllegalArgumentException("Supplied more than 2 argument. Please enter zero or one "
          + "integer only for number of desired clients.");
    }

    //starts the background process of communicating to a master server that keeps track of active
    //servers and updates the active servers on the existence of other servers
    Thread masterServerCommunication = new Thread(new RunServerCommunication(args[0]));
    masterServerCommunication.start();
    System.out.println("MultiChat Server is running...");

    //creates a thread to handle each socket connection to this server,
    ExecutorService pool = Executors.newFixedThreadPool(possibleAmountOfClients);
    try {
      SSLServerSocket server = initSSLDetailsAndGetSocket(args[0]);
      while (true) {
        pool.execute(new Task((SSLSocket)server.accept(), args[0]));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //returns the SSLServerSocket initialized with the keys and algorithm to use for encryption
  private static SSLServerSocket initSSLDetailsAndGetSocket(String portNumber)
      throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException,
      UnrecoverableKeyException, KeyManagementException {

    SSLServerSocketFactory ssf;

    // set up key manager to do server authentication
    SSLContext ctx;
    KeyManagerFactory kmf;
    KeyStore ks;
    char[] passphrase = "socketpractice".toCharArray();

    //specifies TLS protocol, SunX509 key manager algorithm, and .jks keystore file types to be used
    ctx = SSLContext.getInstance("TLS");
    kmf = KeyManagerFactory.getInstance("SunX509");
    ks = KeyStore.getInstance("JKS");

    //loads the keystore to be used for encryption
    ks.load(MultiChatServer.class.getClassLoader().getResourceAsStream(
        "server/resources/keystore/server_keystore.jks"), passphrase);
    kmf.init(ks, passphrase);

    //initializes the SSL context to the key stores with a default security provider and trust store
    ctx.init(kmf.getKeyManagers(), null, null);

    //return a SSL socket with all the specific SSL context
    ssf = ctx.getServerSocketFactory();
    return (SSLServerSocket) ssf.createServerSocket(Integer.parseInt(portNumber));
  }

  //A function object that communicates to the "master" server
  private static class RunServerCommunication implements Runnable {
    private final String portNumber;
    private RunServerCommunication(String portNumber) {
      this.portNumber = portNumber;
    }

    @Override
    public void run() {
      try {
        Socket socketToMasterServer = new Socket("localhost", 51000);
        Scanner serverIn = new Scanner(socketToMasterServer.getInputStream());
        PrintWriter serverOut = new PrintWriter(socketToMasterServer.getOutputStream(), true);

        //tells the master server the name of this server
        serverOut.println("Server " + portNumber);

        //continually listens for the master server's updates on the active server list, then
        //formatting to be an array of server names
        while(serverIn.hasNextLine()) {
          String serverList = serverIn.nextLine();
          serverList = serverList.substring(17);
          String[] activeServers = serverList.split(", ");
          serverNames.clear();
          for (String server : activeServers) {
            serverNames.add(server);
          }
          System.out.println(serverList);
          updateServerList();
        }
        socketToMasterServer.close();
        System.exit(2);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /*
  Represents a "Task" that is run for every client connected to the server per thread.
  Implements Runnable, captures the Socket that is connected and wraps the input and output
  and handles the text input/output of the client.
   */
  private static class Task implements Runnable {

    private String name; //name of client
    private final SSLSocket clientSocket; //the socket of the client connection
    private Scanner in; //the input of the client
    private PrintWriter out; //the output to the client
    private String portNumber;

    //Captures the client's socket as a field.
    private Task(SSLSocket clientSocket, String portNumber) {
      this.clientSocket = clientSocket;
      this.portNumber = portNumber;
    }

    @Override
    public void run() {
      try {
        wrapClientIO();
        requestUsername();
        acceptAndProcessUsername();
        handleUserInput();
      } catch (IOException ioe) {
        System.out.println("Client input/output failed to connect: " + clientSocket.toString());
      } catch (IllegalArgumentException | NoSuchElementException iae) {
        System.out.println(iae.getMessage());
      }
    }

    //Wraps the clients input and outputs streams into a Scanner and PrintWriter respectively.
    private void wrapClientIO() throws IOException {
      in = new Scanner(clientSocket.getInputStream());
      out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    /*
    Requests the username from the client, sending SUBMITNAME. If given a taken or invalid
    username, will send SUBMITANOTHERNAME repeatedly until a valid one has been submit. When a valid
    username has been submitted, it will update the names of the active clients list of the server.
     */
    private void requestUsername() throws IllegalArgumentException, NoSuchElementException {
      String submitNameProtocol = "SUBMITNAME";
      while (true) {
        out.println(submitNameProtocol);
        name = in.nextLine();
        if (name == null) {
          userLeave();
          throw new IllegalArgumentException("Supplied a null name.");
        }
        synchronized (names) {
          if (!name.isBlank() && !names.contains(name) && !name.contains(",") && !name.contains(":")) {
            names.add(name);
            break;
          }
        }
        submitNameProtocol = "SUBMITANOTHERNAME";
      }
    }

    /*
    Tells client that name has been accepted and sends a message to the rest of the clients that
    a new user has joined. Then adds the client's output to the list of client output printwriters.
     */
    private void acceptAndProcessUsername() {
      out.println("NAMEACCEPTED " + name);
      out.println("MESSAGEWELCOME Welcome to Multi-Chat, room "+ portNumber + ", "
          + name + ". Use /help for help!");
      for (PrintWriter writer : outputWriters) {
        writer.println("MESSAGEUSERJOINED " + "[" + new Date().toString() + "] " +
            name + " has joined.");
      }
      outputWriters.add(out);
      updateActiveUsers();
      updateServerList();
      System.out.println("[" + new Date().toString() + "] " + name + " has joined.");
    }

    //transmits user messages to other clients, handles user command requests as a well
    private void handleUserInput() {
      while (true) {
        String input = in.nextLine();
        if (input.toLowerCase().startsWith("/quit")) {
          userLeave();
          return;
        } else if (input.toLowerCase().startsWith("/help")) {
          printHelpMessage();
        } else if (input.toLowerCase().startsWith("/emotes")) {
          printEmoteHelpMessage();
        } else if (input.toLowerCase().startsWith("/join ")) {
          out.println("REQUESTEDNEWROOM " + input.substring(6));
        } else if (input.startsWith("UNSUCCESSFULROOMCHANGE ")) {
          out.println("MESSAGEHELP " + input.substring(23));
        } else {
          for (PrintWriter writer : outputWriters) {
            writer.println("MESSAGE " + "[" + new Date().toString() + "] " + name + ": " +input);
          }
        }
      }
    }

    //handles when a client leaves the chatroom
    private void userLeave() {
      if (out != null) {
        outputWriters.remove(out);
      }
      if (name != null) {
        System.out.println("[" + new Date().toString() + "] " + name + " has left.");
        names.remove(name);
        for (PrintWriter writer : outputWriters) {
          writer.println("MESSAGEUSERLEFT " + "[" + new Date().toString() + "] " + name
              + " has left");
        }
        out.println("Successfully left.");
        updateActiveUsers();
      }
      try {
        clientSocket.close();
      } catch (IOException e) {
        System.out.print("Failure to close client socket: " + clientSocket.toString());
      }
    }

    //prints a help menu with commands to use in MultiChat.
    private void printHelpMessage() {
      out.println("MESSAGEHELP Type /quit to quit MultiChat.");
      out.println("MESSAGEHELP Type /emotes to access a menu of emoticons.");
      out.println("MESSAGEHELP Type /join to join another chat room, "
          + "enter the room number such like: \"/join 59090\".");
      out.println("MESSAGEHELP Type /help to access this help menu.");
    }

    //prints a help menu with commands to produce emoticons
    private void printEmoteHelpMessage() {
      out.println("MESSAGEHELP Emotes menu: ");
      out.println("MESSAGEHELP Smiley Face :) : \":)\"");
      out.println("MESSAGEHELP Frowny Face :( : \":(\"");
      out.println("MESSAGEHELP Ambivalent Face :/ : \":/\"");
      out.println("MESSAGEHELP Excited :D : \":D\"");
      out.println("MESSAGEHELP Despair D: : \"D:\"");
      out.println("MESSAGEHELP Quirky :p :  \":p\"");
      out.println("MESSAGEHELP Pepega( Pepega ) : \"Pepega\"");
      out.println("MESSAGEHELP Pepehands( Pepehands ): \"Pepehands\"");
    }

    //sends the client an updated list of active users
    private void updateActiveUsers() {
      for (PrintWriter writer : outputWriters) {
        StringBuilder activeUserList = new StringBuilder();
        activeUserList.append("ACTIVEUSERLIST ");
        for (String name : names) {
          activeUserList.append(name + ",");
        }
        writer.println(activeUserList.toString());
      }
    }
  }

  //sends the client an updated list of active servers
  private static void updateServerList() {
    for (PrintWriter out : outputWriters) {
      StringBuilder serverList = new StringBuilder();
      serverList.append("ACTIVESERVERLIST ");
      for (String serverName : serverNames) {
        serverList.append(serverName + ",");
      }
      out.println(serverList.toString());
      System.out.println(serverList.toString());
    }
  }
}


