package client.view.swingView;

import client.controller.Features;
import client.view.MultiChatView;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class MultiChatViewImpl extends JFrame implements MultiChatView {

  private Features feature;
  private JTextPane chatLog;
  private JTextPane activeUsers;
  private JTextPane activeServers;
  private JTextArea chatField;
  private StringBuilder log;
  private CountDownLatch latch;
  private boolean startedTyping;
  private JMenuBar menu;

  public MultiChatViewImpl() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    this.setLayout(new FlowLayout());
    log = new StringBuilder();
    startedTyping = false;

    menu = new CustomMenuBar();
    this.setJMenuBar(menu);

    activeUsers = new JTextPane();
    activeUsers.setPreferredSize(new Dimension(100, 350));
    activeUsers.setContentType("text/html");
    activeUsers.setAutoscrolls(true);
    activeUsers.setEditable(false);
    this.add(new JScrollPane(activeUsers));

    CenterPanel center = new CenterPanel();
    this.add(center);

    activeServers = new JTextPane();
    activeServers.setPreferredSize(new Dimension(100, 350));
    activeServers.setContentType("text/html");
    activeServers.setAutoscrolls(true);
    activeServers.setEditable(false);
    this.add(new JScrollPane(activeServers));

    this.pack();
    this.setLocationRelativeTo(null);
    this.setResizable(false);
  }

  @Override
  public void giveFeatures(Features feature) {
    this.feature = feature;
  }

  @Override
  public void display() {
    // if you close the window, it tells the server that you're saying /quit
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent event) {
        feature.sendTextOut("/quit");
        System.exit(3);
      }
    });
    this.setVisible(true);
  }

  @Override
  public void appendChatLog(String s, String color, boolean hasDate) {
    String toAdd = "";
    if (hasDate) {
      toAdd = "<span style=\"color:" + color + "\">" + convertEmoteIfAny(removeHTML(formatDate(s)))
          + "</span><br>";
    } else {
      toAdd = "<span style=\"color:" + color + "\">" + convertEmoteIfAny(removeHTML(s)) +
          "</span><br>";
    }
    log.append(toAdd);
    chatLog.setText(log.toString());
    chatLog.setCaretPosition(chatLog.getDocument().getLength());
  }


  @Override
  public void setTextFieldEditable(boolean b) {
    this.chatField.setEditable(b);
  }

  @Override
  public String getName(String prompt) {
    ScreenNameSelection dialog = new ScreenNameSelection(prompt);
    this.setVisible(false);
    latch = new CountDownLatch(1);
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    this.setTitle("MultiChat - " + dialog.getInput());
    return dialog.getInput();
  }

  @Override
  public void setActiveUsers(List<String> activeUsersList) {
    StringBuilder buildUserList = new StringBuilder();
    buildUserList.append("<h3> Active Users:</h3>");
    for (String user : activeUsersList) {
      buildUserList.append(removeHTML(user) + "<br>");
    }
    activeUsers.setText(buildUserList.toString());
  }

  @Override
  public void setActiveServers(List<String> activeServers) {
    StringBuilder buildServerList = new StringBuilder();
    buildServerList.append("<h3>Active Servers:</h3>");
    for (String server : activeServers) {
      buildServerList.append(server + "<br>");
    }
    this.activeServers.setText(buildServerList.toString());
  }

  private class CenterPanel extends JPanel {

    private CenterPanel() {
      this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      chatLog = new JTextPane();
      chatLog.setContentType("text/html");
      chatLog.setAutoscrolls(true);
      chatLog.setEditable(false);
      JScrollPane scrollChatLog = new JScrollPane(chatLog);
      scrollChatLog.setPreferredSize(new Dimension(400, 300));
      scrollChatLog.setBorder(new LineBorder(Color.BLUE, 1));
      this.add(scrollChatLog);

      chatField = new JTextArea(3, 50);
      chatField.setAutoscrolls(true);
      chatField.setLineWrap(true);
      chatField.setEditable(false);
      chatField.setText("Type here...");
      chatField.addFocusListener(new JTextAreaListener());
      chatField.getDocument().addDocumentListener(new TextAreaDocumentListener());
      this.add(new JScrollPane(chatField));
    }
  }

  private class TextAreaDocumentListener implements DocumentListener {

    @Override
    public void insertUpdate(DocumentEvent e) {
      Document event = e.getDocument();
      try {
        if (event.getText(event.getLength() - 1, 1).equals("\n")) {
          if (event.getLength() > 1) {
            feature.sendTextOut(event.getText(0, event.getLength() - 1));
          }
          SwingUtilities.invokeLater(() -> chatField.setText(""));
        }
      } catch (BadLocationException ble) {
        feature.sendTextOut("/quit");
        MultiChatViewImpl.this.dispose();
        produceErrorMessage("Failure in capturing user input.");
      }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      return;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      return;
    }
  }

  private void produceErrorMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  private class ScreenNameSelection extends JDialog {

    private JLabel prompt;
    private JTextField field;
    private JButton submit;
    private JButton cancel;
    private JPanel buttonPanel;

    private ScreenNameSelection(String prompt) {
      getContentPane().setLayout(
          new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS)
      );

      this.prompt = new JLabel(prompt);
      this.prompt.setAlignmentX(CENTER_ALIGNMENT);
      this.prompt.setPreferredSize(new Dimension(200, 20));
      this.add(this.prompt);

      this.field = new JTextField();
      this.field.setPreferredSize(new Dimension(150, 20));
      this.add(this.field);

      this.buttonPanel = new JPanel();
      this.buttonPanel.setLayout(new FlowLayout());

      this.submit = new JButton("Ok");
      this.submit.addActionListener(e -> confirmName());
      this.buttonPanel.add(submit);

      this.cancel = new JButton("Cancel");
      this.cancel.addActionListener(e -> cancel());
      this.buttonPanel.add(cancel);

      this.add(buttonPanel);

      this.pack();
      this.setLocationRelativeTo(null);
      this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      this.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent event) {
          System.exit(2);
        }
      });
      this.setTitle("Screen name selection");
      this.setVisible(true);
    }

    private void confirmName() {
      latch.countDown();
      this.dispose();
    }

    private void cancel() {
      System.exit(1);
    }

    private String getInput() {
      return this.field.getText();
    }
  }

  private class CustomMenuBar extends JMenuBar {
    private JMenu settings;
    private JMenu view;
    private JMenu help;

    private JMenu switchRooms;
    private JMenu privateMessage;

    private JMenuItem darkmode;
    private JMenuItem font;
    private JMenuItem helpItem;
    private JMenuItem quitItem;

    private CustomMenuBar() {
      settings = new JMenu("MultiChat");
      this.add(settings);
      view = new JMenu("View");
      this.add(view);
      help = new JMenu("Help");
      this.add(help);

      switchRooms = new JMenu("Switch Rooms");
      privateMessage = new JMenu("Private Message...");
      darkmode = new JMenuItem("Enable Darkmode");
      font = new JMenuItem("Font");
      helpItem = new JMenuItem("Help");
      quitItem = new JMenuItem("Quit");

      try {
        Image switchRoomIcon = ImageIO.read(getClass().getResource(
            "/client/resources/images/jmenuicons/switchrooms.png"));
        switchRooms.setIcon(new ImageIcon(switchRoomIcon));
        Image privateMsgIcon = ImageIO.read(getClass().getResource(
            "/client/resources/images/jmenuicons/priv message.png"));
        privateMessage.setIcon(new ImageIcon(privateMsgIcon));
        Image fontIcon = ImageIO.read(getClass().getResource(
            "/client/resources/images/jmenuicons/font.png"));
        font.setIcon(new ImageIcon(fontIcon));
        Image darkModeIcon = ImageIO.read(getClass().getResource(
            "/client/resources/images/jmenuicons/darkmode.png"));
        darkmode.setIcon(new ImageIcon(darkModeIcon));
        Image questionIcon = ImageIO.read(getClass().getResource(
            "/client/resources/images/jmenuicons/question.png"));
        helpItem.setIcon(new ImageIcon(questionIcon));
      } catch (IOException ioe) {
        System.out.print("Failed to open load icon images.");
      }

      settings.add(switchRooms);
      settings.add(privateMessage);
      settings.add(quitItem);
      view.add(darkmode);
      view.add(font);
      help.add(helpItem);

      helpItem.addActionListener(e -> createHelpDialog());
      quitItem.addActionListener(e -> quitFromMenu());
    }
  }
  private void createHelpDialog() {
    new HelpDialog();
  }

  private void quitFromMenu() {
    feature.sendTextOut("/quit");
  }

  private String formatDate(String message) {
    String date = message.substring(0, message.indexOf("]"));
    String[] dateComponents = date.split(" ");
    String month = dateComponents[1];
    String day = dateComponents[2];
    String time = dateComponents[3];
    String timezone = dateComponents[4];

    StringBuilder buildDate = new StringBuilder();
    buildDate.append("[");
    buildDate.append(month);
    buildDate.append(" ");
    buildDate.append(day);
    buildDate.append(" ");
    buildDate.append(time);
    buildDate.append(" ");
    buildDate.append(timezone);
    buildDate.append("]");
    buildDate.append(message.substring(message.indexOf("]") + 1));

    return buildDate.toString();
  }

  private class JTextAreaListener implements FocusListener {

    @Override
    public void focusGained(FocusEvent e) {
      if(chatField.getText().equals("Type here...") && !startedTyping) {
        chatField.setText("");
        startedTyping = true;
      }
    }

    @Override
    public void focusLost(FocusEvent e) {
      return;
    }
  }

  private String removeHTML(String str) {
    str = str.replaceAll("<","&lt;");
    str = str.replaceAll(">","&gt;");
    return str;
  }

  private String convertEmoteIfAny(String msg) {
    StringBuilder builder = new StringBuilder();

    // split message by space
    String[] words = msg.split(" ");

    for(String word : words) {
      // if the word equals an emoji name (ex. <3) then replace it with html image code
      String currentWord = word.trim();
      if(MultiChatView.HTMLEMOTES.containsKey(currentWord)) {
        builder.append("<img src = \"" + MultiChatViewImpl.class.getClassLoader()
            .getResource("client/resources/images/emojis/" + MultiChatView.HTMLEMOTES.get(
                currentWord)).toString() + "\" alt = \"error\" width = \"20\" height = \"20\">");
      } else if(MultiChatView.TWITCH_EMOTES.containsKey(currentWord)) {
        builder.append("<img src = \"" + MultiChatViewImpl.class.getClassLoader()
            .getResource("client/resources/images/twitch/" + MultiChatView.TWITCH_EMOTES.get(
                currentWord)).toString() + "\" alt = \"error\" width = \"40\" height = \"40\">");
      } else {
        builder.append(word);
      }

      // add the space back in (got removed when splitting)
      builder.append(" ");
    }

    return builder.toString();
  }
}
