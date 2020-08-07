package client.view;

import static java.util.Map.entry;

import client.controller.Features;
import java.util.List;
import java.util.Map;

public interface MultiChatView {

  Map<String, String> HTMLEMOTES = Map.ofEntries(
      entry("&lt;3", "heart.png"),
      entry(":)", "smiley.png"),
      entry(":(", "frowny.png"),
      entry(":/", "confused.png"),
      entry(":D", "excited.png"),
      entry("D:", "anguish.png"),
      entry(":p", "tongue.png"),
      entry("&gt;:(", "angry.png")
  );

  Map<String, String> FXEMOTES = Map.ofEntries(
          entry("<3", "heart.png"),
          entry(":)", "smiley.png"),
          entry(":(", "frowny.png"),
          entry(":/", "confused.png"),
          entry(":D", "excited.png"),
          entry("D:", "anguish.png"),
          entry(":p", "tongue.png"),
          entry(">:(", "angry.png")
  );

  Map<String, String> TWITCH_EMOTES = Map.ofEntries(
      entry("Pepehands", "Pepehands.png"),
      entry("Pepega", "Pepega.png"),
      entry("Kappa", "Kappa.png"),
      entry("forsenCD", "forsenCD.jpg"),
      entry("pepeD", "pepeD.gif"),
      entry("pepoG", "pepoG.png"),
      entry("WeirdChamp", "WeirdChamp.png"),
      entry("widepeepohappy", "widepeepohappy.png"),
      entry("FeelsBadMan", "FeelsBadMan.png"),
      entry("FeelsGoodMan", "FeelsGoodMan.png"),
      entry("Pog", "Pog.png"),
      entry("OMEGALUL", "OMEGALUL.png"),
      entry("KEKW", "KEKW.png"),
      entry("monkaHmm", "monkaHmm.png")
  );

  void giveFeatures(Features feature);

  void display();

  void appendChatLog(String s, String color, boolean hasDate);

  void setTextFieldEditable(boolean b);

  String getName(String prompt);

  void setActiveUsers(List<String> activeUsers);

  void setActiveServers(List<String> activeServers);

  void dispose();
}
