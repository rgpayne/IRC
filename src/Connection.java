import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Connection implements Runnable {

    public static final String CTCP_DELIM = "\001", CTCP_BOLD_DELIM = "\002", CTCP_COLOR_DELIM = "\003",
            CTCP_UNDERLINE_DELIM = "\037", CTCP_RESET_DELIM = "\017", HTTP_DELIM = "\004";
    static String[] nicks = {"", "", ""};
    static String currentNick = "", real = "";
    static DnDTabbedPane tabbedPane;
    static JLabel tabInfo;
    Thread thread;
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    String server, password, title;
    boolean doneUpdating = false, currentlyUpdating = false;
    boolean autoconnect = true;
    int port;
    ArrayList<ListChannel> channelList = new ArrayList<>();
    boolean isConnected;



    public Connection(String title, String server, int port) {
        this.title = title;
        this.server = server;
        this.port = port;

        thread = new Thread(this);
        thread.start();
    }


    public Connection(String title, String server, int port, boolean autoconnect) {
        this.title = title;
        this.server = server;
        this.port = port;
        this.autoconnect = autoconnect;
        thread = new Thread(this);
        thread.start();
    }



    /** Finds the tab with the given title on the given connection */
    public static int findTab(String title, Connection conn) {
        int totalTabs = tabbedPane.getTabCount();
        for (int i = 0; i < totalTabs; i++) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);

            String channelName = channel.name;
            Connection c = channel.connection;
            String serverTitle = conn.title;
            if (conn == c && serverTitle.equals(title)) return i;
            if (channelName.equalsIgnoreCase(title) && conn == c) return i;
            if (conn == c && channelName.equalsIgnoreCase("")) return i;
        }
        return -1;
    }




    /** Used when chat input begins with a forwardslash to send the message directly to the server */
    public void send(String line) throws IOException, BadLocationException {
        if (line.toUpperCase().equals("QUIT")) {
            this.writer.write("QUIT :"+GUI.quitMessage+"\r\n");
            this.writer.flush();
            return;
        }
        this.writer.write(line + "\r\n");
        this.writer.flush();

    }




    public boolean checkForCTCPDelims(String line) {
        return line.contains(CTCP_BOLD_DELIM) || line.contains(CTCP_COLOR_DELIM) || line.contains(CTCP_DELIM) || line.contains(CTCP_UNDERLINE_DELIM);
    }




    /** Checks server messages and routes the message to a channelPanel */
    public void parseFromServer(String line) throws IOException, BadLocationException {
        final Parser parser = new Parser(line);
        String command = parser.getCommand();
       // System.out.println(parser.toString());

        if (command.equals("AWAY")) {
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab("#" + channelName, this);
            String[] msg = {null, parser.getTrailing()};
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("ERROR")) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            if (channel == null) {
                ChannelPanel cp = new ChannelPanel(Connection.this.title, "", Connection.this);
                String[] msg = {null, parser.getTrailing()};
                cp.insertString(msg, GUI.connectStyle, false);
                return;
            }
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("INVITE")) {
            String nick = parser.getNick();
            String chan = parser.getTrailing();
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, nick + " has invited you to " + chan + "."};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("JOIN")) {
            if (currentNick.equals(parser.getNick())) //if joined is me
            {
                String channelName = parser.getTrailing();
                if (channelName.equals("")) {
                    channelName = parser.getParams().trim();
                }
                if (channelName.startsWith("#")) {
                    int indexOfChannel = findTab(channelName, this);
                    if (indexOfChannel == -1) {


                        final String channelName2 = channelName;

                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {

                                @Override
                                public void run() {

                                    try {
                                        ChannelPanel channel = new ChannelPanel(channelName2, channelName2, Connection.this);
                                        String[] msg = {null, "You (" + parser.getPrefix() + ") have joined " + parser.getParams().trim().substring(1) + "."};
                                        channel.insertString(msg, GUI.connectStyle, false);
                                    } catch (BadLocationException | IOException ex) {
                                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                                    }

                                }
                            });
                            return;
                        } catch (InterruptedException | InvocationTargetException ex) {
                            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else { //joining a room for which you already have a tab (i.e. you were kicked or lost connection or something)

                        ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
                        String[] msg = {null, "Joined " + channelName};
                        channel.insertString(msg, GUI.serverStyle, false);
                    }
                }
            } else //if joined isn't me
            {
                String channelName = parser.getTrailing();
                if (channelName.startsWith("#")) {
                    int indexOfChannel = findTab(channelName, this);
                    if (indexOfChannel != -1) {
                        ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
                        channel.addToUserList(parser.getNick());
                        String[] msg = {null, "--> " + parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() + ") has joined the channel."};
                        channel.insertString(msg, GUI.joinStyle, false);
                        return;
                    }
                }
            }
        }
        if (command.equals("KICK")) {
            String middle = parser.getMiddle();
            String channelName = middle.substring(0, middle.indexOf(" "));
            String kicked = middle.substring(middle.indexOf(" ")).trim();
            String kickedBy = parser.getNick();
            String kickMessage = parser.getTrailing();

            int indexOfChannel = findTab(channelName, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);


            if (kicked.equals(currentNick)) //i am kicked
            {
                String[] msg = {null, "*** You have been kicked from the channel by " + kickedBy + " (" + kickMessage + ")"};
                channel.insertString(msg, GUI.serverStyle, false);
                channel.clear();
                return;
            }
            if (kickedBy.equals(currentNick)) //i kick somebody
            {
                String[] msg = {null, "*** You have kicked " + kicked + " from the channel (" + kickMessage + ")"};
                channel.insertString(msg, GUI.serverStyle, false);
                channel.removeFromUserList(kicked);
                return;
            } else //somebody else kicked
            {
                String[] msg = {null, "*** " + kicked + " was kicked from the channel (" + kickMessage + ")"};
                channel.insertString(msg, GUI.disconnectStyle, false);
                channel.removeFromUserList(kicked);
                return;
            }
        }
        if (command.equals("MODE")) {
            if (parser.getServer().equals(currentNick) || parser.getPrefix().equals(currentNick)) //setting personal mode
            {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                String[] s = parser.getParams().trim().split(" ");
                String modes;
                if (s.length == 2) modes = s[1];
                else modes = parser.getTrailing();
                String[] msg = {null, "You have set personal modes: " + modes};
                channel.insertString(msg, GUI.serverStyle, false);
                return;
            } else //setting a user's mode
            {
                String[] s = parser.getParams().trim().split(" ");
                String receiver = "";
                String giver = parser.getNick();
                String chan = s[0];
                String power = s[1];
                if (s.length >= 3) receiver = s[2];

                int indexOfChannel = findTab(chan, this);
                if (indexOfChannel == -1) return;
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);

                if (!receiver.equals("")) {
                    String[] msg = {null, "*** " + parser.getParams().trim()};
                    channel.insertString(msg, GUI.serverStyle, false);
                    channel.removeFromUserList(receiver);

                    if (power.equals("+o") || power.equals("+v") || power.equals("+a") || power.equals("+h") || power.equals("+q") ||
                            power.equals("-o") || power.equals("-v") || power.equals("-a") || power.equals("-h") || power.equals("-q")) {
                        String newNick = receiver;
                        if (power.equals("+o")) newNick = "@" + receiver; //operator
                        if (power.equals("+v")) newNick = "+" + receiver; //voice
                        if (power.equals("+a")) newNick = "&" + receiver; //admin
                        if (power.equals("+h")) newNick = "%" + receiver; //half-op
                        if (power.equals("+q")) newNick = "~" + receiver; //owner
                        if (newNick.equals(receiver)) newNick = " " + receiver;
                        channel.addToUserList(newNick);
                    }
                    return;
                }
                if (giver.equals("")) {
                    String[] ss = {null, "*** " + "Channel mode set to: " + power};
                    channel.insertString(ss, GUI.serverStyle, false); //channel mode set by nobody
                    return;
                } else {
                    String[] ss = {null, "*** " + giver + " set the channel to: " + power};
                    channel.insertString(ss, GUI.serverStyle, false); //channel mode
                    return;
                }
            }
        }
        if (command.equals("NICK")) {
            String prefix = parser.getNick().substring(0, 1);
            String oldNick = parser.getNick();
            String newNick = parser.getTrailing();

            if (!currentNick.equals(oldNick)) //if someone else changes name
            {
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                    if (channel.contains(" " + oldNick)) {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList(" " + newNick);
                        String[] msg = {null, "*** " + oldNick + " is now known as " + newNick + "."};
                        channel.insertString(msg, GUI.serverStyle, false);
                        return;
                    }
                    if (channel.contains("@" + oldNick)) {
                        String[] msg = {null, "*** " + oldNick + " is now known as " + newNick + "."};
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("@" + newNick);
                        channel.insertString(msg, GUI.serverStyle, false);
                        return;
                    }
                    if (channel.contains("~" + oldNick)) {
                        String[] msg = {null, "*** " + oldNick + " is now known as " + newNick + "."};
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("~" + newNick);
                        channel.insertString(msg, GUI.serverStyle, false);
                        return;
                    }
                    if (channel.contains("+" + oldNick)) {
                        String[] msg = {null, "*** " + oldNick + " is now known as " + newNick + "."};
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("+" + newNick);
                        channel.insertString(msg, GUI.serverStyle, false);
                        return;
                    }
                    if (channel.contains("%" + oldNick)) {
                        String[] msg = {null, "*** " + oldNick + " is now known as " + newNick + "."};
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("%" + newNick);
                        channel.insertString(msg, GUI.serverStyle, false);
                        return;
                    }
                    if (channel.contains("&" + oldNick)) {
                        String[] msg = {null, "*** " + oldNick + " is now known as " + newNick + "."};
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("&" + newNick);
                        channel.insertString(msg, GUI.serverStyle, false);
                        return;
                    }
                }
                return;
            } else //if you change your name
            {
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    String[] msg = {null, "*** You are now known as " + newNick};
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                    channel.removeFromUserList(oldNick);
                    Connection.currentNick = prefix + newNick.substring(1);
                    channel.addToUserList(Connection.currentNick);
                    channel.insertString(msg, GUI.serverStyle, false);
                }
                return;
            }
        }
        if (command.equals("NOTICE")) {
            boolean ctcp = checkForCTCPDelims(line);
            String h = parser.getPrefix();
            int indexOfChannel = findTab(Connection.this.title, this);
            ChannelPanel channel;

            if (indexOfChannel == -1) {
                channel = new ChannelPanel(title, "", Connection.this);
            } else channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);

            String nick = parser.getNick();

            if (!nick.equals("")) {
                String[] msg = {null, "-" + nick + "- " + parser.getTrailing()};

                if (msg[1].contains("PING")) {
                    String ping = parser.getTrailing().substring(6).trim();
                    long ping1;
                    Long longTime = System.currentTimeMillis() / 1000L;
                    if (StringUtils.isNumeric(ping)) {
                        ping1 = Long.valueOf(ping);
                        long ping2 = longTime - ping1;
                        msg[1] = "Received CTCP-Ping reply from " + nick + ": " + ping2 + " seconds."; //ms or s?
                    }
                }
                channel.insertString(msg, GUI.connectStyle, ctcp);
            } else {
                String[] msg = {null, parser.getTrailing()};
                channel.insertString(msg, GUI.connectStyle, ctcp);
                if (!h.equals("")) channel.server = parser.getPrefix();
            }

            return;
        }
        if (command.equals("PART")) {
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);

            int indexOfChannel = findTab(channelName, this);

            if (currentNick.equals(parser.getNick())) { //if i'm leaving
                tabbedPane.remove(indexOfChannel);
                return;
            } else {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
                String[] msg = {null, "<-- " + parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() + ") has left the channel (" + parser.getTrailing() + ")"};
                channel.insertString(msg, GUI.disconnectStyle, false);
                channel.removeFromUserList(parser.getNick());
                return;
            }
        }
        if (command.equals("PING")) {
            send("PONG :" + parser.getTrailing());
            return;
        }
        if (command.equals("PRIVMSG") || command.equals("MSG")) {
            final boolean ctcp = checkForCTCPDelims(line);
            if (parser.getTrailing().startsWith(CTCP_DELIM)) {
                if (parser.getTrailing().substring(1).startsWith("ACTION")) //CTCP action
                {
                    String channelName = parser.getMiddle();
                    int indexOfChannel = findTab(channelName, this);
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
                    String rest = parser.getTrailing().substring(7);
                    String[] s = {parser.getNick(), rest};
                    channel.insertCTCPAction(s);
                    return;
                }
                if (parser.getTrailing().substring(1).startsWith("CLIENTINFO")) //CTCP clientinfo
                {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                    String[] msg = {null, "Received CTCP-ClientInfo request from " + parser.getNick() + "."};
                    channel.insertString(msg, GUI.connectStyle, false);
                    this.send("NOTICE " + parser.getNick() + " :\001CLIENTINFO CTCP commands: ACTION FINGER PING SOURCE TIME USERINFO VERSION\001");
                    return;
                }
                if (parser.getTrailing().substring(1).startsWith("FINGER")) //CTCP Finger
                {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                    String[] msg = {null, "Received CTCP-Finger request from " + parser.getNick() + "."};
                    channel.insertString(msg, GUI.connectStyle, false);
                    this.send("NOTICE " + parser.getNick() + " :\001FINGER " + ChannelPanel.CTCPFingerMessage + "\001");
                    return;
                }
                if (parser.getTrailing().substring(1).startsWith("PING")) //CTCP Ping
                {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                    String[] msg = {null, "Recieved CTCP-Ping request from " + parser.getNick() + "."};
                    channel.insertString(msg, GUI.connectStyle, false);
                    this.send("NOTICE " + parser.getNick() + " :" + parser.getTrailing());
                    return;
                }
                if (parser.getTrailing().substring(1).startsWith("SOURCE")) //CTCP source
                {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                    String[] msg = {null, "Received CTCP-Source request from " + parser.getNick() + "."};
                    channel.insertString(msg, GUI.connectStyle, false);
                    this.send("NOTICE " + parser.getNick() + " :\001SOURCE Unavailable\001");
                    return;
                }
                if (parser.getTrailing().substring(1).startsWith("TIME")) //CTCP Time
                {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                    String[] msg = {null, "Received CTCP-Time request from " + parser.getNick() + "."};
                    channel.insertString(msg, GUI.connectStyle, false);
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm aa");
                    sdf.setTimeZone(TimeZone.getTimeZone("CST"));
                    String time = sdf.format(date);
                    this.send("NOTICE " + parser.getNick() + " :\001TIME " + time + "\001");
                    return;
                }
                if (parser.getTrailing().substring(1).startsWith("USERINFO")) //ctcp userinfo
                {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                    String[] msg = {null, "Received CTCP-UserInfo request from " + parser.getNick() + "."};
                    channel.insertString(msg, GUI.connectStyle, false);
                    this.send("NOTICE " + parser.getNick() + " :\001USERINFO " + ChannelPanel.CTCPUserInfo + "\001");
                    return;
                }
                if (parser.getTrailing().substring(1).startsWith("VERSION")) //CTCP Version
                {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                    String[] msg = {null, "Received CTCP-Version request from " + parser.getNick() + "."};
                    channel.insertString(msg, GUI.connectStyle, false);
                    this.send("NOTICE " + parser.getNick() + " :\001VERSION AlphaClient:v0.1:LM17\001");  //placeholder
                    return;
                } else {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                    String[] msg = {null, "Received invalid CTCP request from " + parser.getNick() + ": " + parser.getTrailing().substring(1)};
                    channel.insertString(msg, GUI.connectStyle, false);
                    return;
                }
            }

            //STANDARD PRIV MESSAGE
            String channelName = parser.getMiddle();
            if (channelName.equals(currentNick)) {
                channelName = parser.getNick();
                final String channelName2 = channelName;
                int indexOfChannel = findTab(channelName, this);
                if (indexOfChannel == -1) {


                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {

                                ChannelPanel channel = null;
                                try {
                                    channel = new ChannelPanel(channelName2, channelName2, Connection.this);
                                } catch (BadLocationException | IOException ex) {
                                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                channel.setRightComponent(null);
                                channel.setDividerSize(0);
                                String[] msg = {channelName2, parser.getTrailing()};
                                try {
                                    channel.insertString(msg, GUI.chatStyle, ctcp);
                                } catch (BadLocationException | IOException ex) {
                                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });


                        return;
                    } catch (InterruptedException | InvocationTargetException ex) {
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
                String[] msg = {parser.getNick(), parser.getTrailing()};
                channel.insertString(msg, GUI.chatStyle, ctcp);
                return;
            }
            int indexOfChannel = findTab(channelName, this);
            if (indexOfChannel == -1) {
                final String channelName2 = channelName;
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {

                            try {
                                ChannelPanel channel = new ChannelPanel(channelName2, channelName2, Connection.this);
                                String[] msg = {channelName2, parser.getTrailing()};
                                channel.insertString(msg, GUI.chatStyle, ctcp);
                            } catch (BadLocationException | IOException ex) {
                                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }

                    });
                } catch (InterruptedException | InvocationTargetException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }

                return;
            }
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            String[] msg = {parser.getNick().trim(), parser.getTrailing()};
            channel.insertString(msg, GUI.chatStyle, ctcp);
            return;
        }
        if (command.equals("QUIT")) {
            boolean ctcp = checkForCTCPDelims(line);
            String quitter = parser.getNick().trim();
            String quitMessage = parser.getParams().substring(2); //fix so it only quits on connection user quit on?
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                int index  = findTab(channel.title, this);
                if (index != -1) {
                    boolean success = channel.removeFromUserList(quitter);
                    String[] msg = {null, "<-- " + quitter + " has left the server (" + quitMessage + ")"};
                    if (success) {
                        channel.insertString(msg, GUI.disconnectStyle, ctcp);

                    }
                }
            }
            return;
        }
        if (command.equals("TOPIC")) {
            boolean ctcp = checkForCTCPDelims(line);
            String channelName = parser.getMiddle();
            int indexOfChannel = findTab(channelName, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            channel.topic = parser.getTrailing();

            if (currentNick.equals(parser.getNick())) {
                String[] msg = {null, "*** You set the channel topic to: " + channel.topic};
                channel.insertString(msg, GUI.serverStyle, ctcp);
            } else {
                String[] msg = {null, parser.getNick() + " has changed the topic to: " + channel.topic};
                channel.insertString(msg, GUI.serverStyle, ctcp);
            }
            return;

        }
        if (command.equals("001") || command.equals("002") || command.equals("003") || command.equals("004") || command.equals("005")) {
            boolean ctcp = checkForCTCPDelims(line);
            final String host = parser.getPrefix();
            int indexOfChannel = findTab(Connection.this.title, this);
            if (indexOfChannel == -1) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                ChannelPanel c = new ChannelPanel(Connection.this.title, host, Connection.this);
                                c.server = parser.getPrefix();
                            } catch (BadLocationException | IOException ex) {
                                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                } catch (InterruptedException | InvocationTargetException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            indexOfChannel = findTab(Connection.this.title, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            if (channel.name.equals("")) channel.name = host;
            String text = parser.getTrailing();
            if (text.equals("")) text = parser.getParams().trim().substring(currentNick.length()).trim();
            String[] msg = {null, text};
            channel.insertString(msg, GUI.connectStyle, ctcp);

            if (command.equals("001") && this.autoconnect) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (int i = 0; i < GUI.savedConnections.size(); i++) {
                    SavedConnection conn = GUI.savedConnections.get(i);
                    if (conn.getServer().equals(channel.connection.server)) {
                        ArrayList<String> c = conn.retrieveChannels();
                        if (c.isEmpty()) return;
                        for (String chan : c) {
                            if (chan.isEmpty()) return;
                            send("JOIN " + chan);
                        }
                    }
                }
            }
            return;
        }
        if (command.equals("042")) //unique id
        {
            int index = findTab(Connection.this.title, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(index);
            String[] msg = {null, parser.getParams().trim() + "."};
            channel.insertString(msg, GUI.connectStyle, false);
            return;
        }
        if (command.equals("219")) //end of /STATS
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            if (channel != null) channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("221")) //requesting to see own modes (/modes rieux)
        {
            int indexOfChannel = findTab(Connection.this.title, this);
            ChannelPanel channel;
            if (indexOfChannel == -1) channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            else channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);

            String[] s = parser.getParams().trim().split(" ");
            String modes = s[1];
            String[] msg = {null, "Your personal modes are: " + modes + "."};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("242")) //uptime
        {
            boolean ctcp = checkForCTCPDelims(line);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.serverStyle, ctcp);
            return;
        }
        if (command.equals("249")) //  /stats p
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            if (channel != null) channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("251") || command.equals("255")) {
            boolean ctcp = checkForCTCPDelims(line);
            String p = parser.getParams();
            String message = p.substring(p.indexOf(":") + 1);

            int indexOfChannel = findTab(Connection.this.title, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            String[] msg = {null, message + "."};
            channel.insertString(msg, GUI.connectStyle, ctcp);
            return;
        }
        if (command.equals("252") || command.equals("253") || command.equals("254")) {
            boolean ctcp = checkForCTCPDelims(line);
            String[] s = parser.getParams().trim().split(" ");
            String digit = s[1];
            String msg = parser.getParams().substring(parser.getParams().indexOf(":") + 1);

            int indexOfChannel = findTab(Connection.this.title, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            String[] input = {null, digit + " " + msg + "."};
            channel.insertString(input, GUI.connectStyle, ctcp);
            return;
        }
        if (command.equals("256") || command.equals("257") || command.equals("258") || command.equals("259")) {
            boolean ctcp = checkForCTCPDelims(line);
            int indexOfChannel = findTab(Connection.this.title, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.connectStyle, ctcp);
            return;
        }
        if (command.equals("263")) //server load too heavy. please try again (happens with /list)
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("265") || command.equals("266")) {
            boolean ctcp = checkForCTCPDelims(line);
            int indexOfChannel = findTab(Connection.this.title, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.connectStyle, ctcp);
            return;
        }
        if (command.equals("301")) //received when target of whois, privmsg, etc is set to away
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, target + " is away: " + info};
            channel.insertString(msg, GUI.serverStyle, false);
        }
        if (command.equals("302")) //userhost
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("303")) //ison
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String name = "";
            if (!parser.getTrailing().equals("")) name = parser.getTrailing();
            if (name.equals("")) return;
            String[] msg = {null, "Online: " + name};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("305")) //no longer away
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, "You are no longer marked as away."};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("306")) //away
        {
            boolean ctcp = checkForCTCPDelims(line);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, "You have been marked as away (Reason: " + GUI.awayMessage + ")"};
            channel.insertString(msg, GUI.serverStyle, ctcp);
            return;
        }
        if (command.equals("307")) //userip
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, target + " " + info};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("310")) //whoishelpop
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String modes = parser.getTrailing();
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, target + " " + modes};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("311")) //Whois user
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String fulladd = s[2] + "@" + s[3] + " (" + parser.getTrailing().trim() + ")";
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, target + " is " + fulladd};
            channel.insertString(msg, GUI.serverStyle, false);
            return;

        }
        if (command.equals("312")) //whois server
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            String srv = parser.getPrefix();
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, target + " is online via " + srv + " (" + info + ")"};
            channel.insertString(msg, GUI.serverStyle, false);
            return;

        }
        if (command.equals("313")) //whois operator
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, target + " is an IRC Operator"};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("314")) //whowas user
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("315")) //end of /who
        {
            String s = parser.getMiddle().split(" ")[1];
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, "End of /WHO list for " + s};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("317")) //whois idletime
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String idleTime = s[2];
            String time = s[3];

            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();

            Date date = new Date(Integer.valueOf(time) * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm aa z");
            sdf.setTimeZone(TimeZone.getTimeZone("CST"));
            channel.signOnTime = sdf.format(date);
            String[] msg1 = {null, target + " has been idle for " + idleTime + " seconds"};
            String[] msg2 = {null, target + " has been online since " + channel.signOnTime};
            channel.insertString(msg1, GUI.serverStyle, false);
            channel.insertString(msg2, GUI.serverStyle, false);

        }
        if (command.equals("318")) //end of whois
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("319")) //whois channels
        {
            String[] s = parser.getParams().split(" ");
            String target = s[2];
            String chans = parser.getTrailing();
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, target + " is a user on channels: " + chans};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("321")) // /list start
        {
            currentlyUpdating = true;
            if (!channelList.isEmpty()) channelList.clear();
            return;
        }
        if (command.equals("322")) //list
        {
            String[] s = parser.getMiddle().split(" ");
            String channelName;
            int pop = 0;

            if (s.length > 2 && StringUtils.isNumeric(s[2])) {
                channelName = s[1];
                pop = Integer.valueOf(s[2]);
            } else {
                String[] p = parser.getParams().trim().split(" ");
                channelName = p[1];
                if (StringUtils.isNumeric(p[2])) pop = Integer.valueOf(p[2]);
            }
            String channelInfo = parser.getTrailing();

            ListChannel chan = new ListChannel(channelName, channelInfo, pop);
            channelList.add(chan);
            return;
        }
        if (command.equals("323")) //end of list
        {
            doneUpdating = true;
            currentlyUpdating = false;
            return;
        }
        if (command.equals("324")) //channel mode
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] s = parser.getParams().trim().split(" ");
            String[] msg = {null, s[1] + " modes: " + s[2]};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("329")) //channel creation time
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] s = parser.getParams().trim().split(" ");
            long time = Long.valueOf(s[2]);
            Date date = new Date(time * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy 'at' hh:mm a");
            sdf.setTimeZone(TimeZone.getTimeZone("CST"));
            String formattedDate = sdf.format(date);
            String[] msg = {null, s[1] + " was created on " + formattedDate};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("331")) //no topic
        {
            String[] m = parser.getMiddle().split(" ");
            String channelName = m[1];
            String[] msg = {null, parser.getTrailing()};
            int channelIndex = findTab(channelName, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(channelIndex);
            channel.topic = "";
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("332")) //topic
        {
            boolean ctcp = checkForCTCPDelims(line);
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);

            int indexOfChannel = findTab(channelName, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            channel.topic = parser.getTrailing();
            channel.server = parser.getPrefix();
            String[] msg = {null, "Current Topic: " + channel.topic};
            channel.insertString(msg, GUI.serverStyle, ctcp);
            return;
        }
        if (command.equals("333")) //time of topic change and who set it
        {
            String[] s = parser.getParams().trim().split(" ");
            String setter = s[2], channelName = s[1], rawTime = s[3].trim();
            long time = -1;
            if (StringUtils.isNumeric(s[3])) {
                time = Long.parseLong(rawTime);
            }
            Date date = new Date(time * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy 'at' hh:mm a");
            sdf.setTimeZone(TimeZone.getTimeZone("CST"));
            String formattedDate = sdf.format(date);
            String[] msg = {null, "The topic was set by " + setter + " on " + formattedDate + "."};
            int indexOfChannel = findTab(channelName, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("338")) //whois actually
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, target + " " + info};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("341")) //successfal invite send
        {
            String[] s = parser.getParams().trim().split(" ");
            String[] msg = {null, "You have invited " + s[1] + " to " + s[2] + "."};
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("351")) //version irc server is running
        {
            String s = parser.getParams().trim();
            String[] msg = {null, s};
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("352")) //who reply
        {
            String[] s = parser.getMiddle().split(" ");
            String person;
            if (s.length >= 5) person = s[5];
            else {
                String[] p = parser.getParams().split(" ");
                person = p[5];
            }
            String[] msg = {null, person + " is " + s[2] + "@" + s[3] + " (" + parser.getTrailing().substring(2) + ")"};
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            channel.insertString(msg, GUI.serverStyle, false);

        }
        if (command.equals("353")) //names command
        {
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);
            int indexOfChannel = findTab(channelName, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            String[] nn = parser.getTrailing().split(" ");
            for (int i = 0; i < nn.length; i++) {
                String name = nn[i];
                char first = name.charAt(0);
                if (first == '@' || first == '+' || first == '%' || first == '~' || first == '&') continue;
                else nn[i] = " " + name;
            }
            channel.list.addAll(Arrays.asList(nn));
            return;
        }
        if (command.equals("364")) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getParams().trim()};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("365")) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("366")) //end of names command
        {
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);
            int indexOfChannel = findTab(channelName, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);

            for (String nextElement : channel.list) {
                channel.addManyToUserList(nextElement);
            }
            channel.list.clear();
            channel.server = parser.getPrefix();
            channel.fireIntervalAdded();
            return;
        }
        if (command.equals("367")) //banlist 10 7 5
        {
            String[] s = parser.getParams().trim().split(" ");
            Long time = Long.valueOf(s[4]);
            Date date = new Date(time * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy 'at' hh:mm a");
            sdf.setTimeZone(TimeZone.getTimeZone("CST"));
            String formattedDate = sdf.format(date);
            String[] msg = {null, s[1] + ": " + CTCP_COLOR_DELIM + 5 + s[2] + CTCP_RESET_DELIM + " on " + CTCP_COLOR_DELIM + 10 + formattedDate + CTCP_RESET_DELIM + " by " + CTCP_COLOR_DELIM + 7 + s[3]};

            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            channel.insertString(msg, GUI.chatStyle, true);
            return;
        }
        if (command.equals("368")) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.chatStyle, false);
            return;
        }
        if (command.equals("369")) //end of whowas
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("371") || command.equals("372") || command.equals("374") || command.equals("375") || command.equals("376")) {
            boolean ctcp = checkForCTCPDelims(line);
            String host = parser.getPrefix();
            int indexOfChannel = findTab(Connection.this.title, this);
            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(indexOfChannel);
            if (command.equals("372")) channel.server = host;
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.connectStyle, ctcp);
            return;
        }
        if (command.equals("391")) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getMiddle() + " " + parser.getTrailing()};
            if (channel != null) channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("401")) //no such nick/channel
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, s[1] + ": No such nick/channel."};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("402")) //no such server
        {
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, "No such server."};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("403")) //no such channel 
        {
            String[] s = parser.getParams().split(" ");
            String chan = s[1].trim();
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, chan + ": No such channel. "};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("404")) {
            String[] s = parser.getParams().split(" ");
            String chan = s[1].trim();
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, chan + ": Cannot send to channel."};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("405")) //joined too many channels
        {
            String[] s = parser.getParams().trim().split(" ");
            String chan = s[1].trim();
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, chan + ": You have joined too many channels."};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("406")) //there was no such nickname (whowas)
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("407") || command.equals("408")) // No such server / too many targets 
        {                                                   //PLACEHOLDERS
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, "*** " + parser.getMiddle().trim() + ": " + parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("409") || command.equals("411")) //there was no such nickname (whowas)
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("412")) //no text to send
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, s[0] + " no text to send."};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("413") || command.equals("414") || command.equals("415")) // No top level / wild top level / bad mask
        {                                                   //PLACEHOLDERS
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, "*** " + parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("421")) //unknown command
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, s[1] + ": Unknown command."};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("422") || command.equals("423") || command.equals("431"))
        //error no motd / error no admin info / error no nick given / Error erroneous nick
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            if (channel != null) channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("432")) //eroneous nickname
        {
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, "Erroneous Nickname"};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("433")) //nick in use
        {
            String[] s = parser.getParams().trim().split(" ");
            String newNick = "";
            for (int i = 0; i < nicks.length; i++) {
                if (s[1].equals(nicks[i])) {
                    if (i < 2) newNick = nicks[i + 1];
                }
            }
            Connection.currentNick = newNick;
            if (currentNick.equals("")) {
                ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
                String[] msg = {null, "All your nicks are taken"};
                channel.insertString(msg, GUI.errorStyle, false);
                channel.connection.send("quit");
                return;
            }
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, "**" + parser.getParams().trim()};
            channel.insertString(msg, GUI.errorStyle, false);
            channel.connection.send("NICK " + currentNick);
            return;
        }
        if (command.equals("436")) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, "Error: Nickname collision"};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("437")) //cannot change nickname while banned or moderated on channel
        {
            String[] s = parser.getParams().trim().split(" ");
            String chan = s[1].trim();
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, chan + ": You have joined too many channels."};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("438")) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("439")) //please wait until we process your connection
        {
            int indexOfChannel = findTab(Connection.this.title, this);
            if (indexOfChannel == -1) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new ChannelPanel(title, parser.getPrefix(), Connection.this);
                            } catch (BadLocationException | IOException ex) {
                                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    indexOfChannel = findTab(Connection.this.title, this);
                } catch (InterruptedException | InvocationTargetException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getComponentAt(indexOfChannel));
            channel.server = parser.getPrefix();
            String[] msg = {null, "Please wait while we process your connection..."};
            channel.insertString(msg, GUI.connectStyle, false);
            return;
        }
        if (command.equals("441")) //error user not in channel
        {
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] s = parser.getMiddle().split(" ");
            String[] msg = {null, "*** " + s[1] + " " + s[2] + ": " + parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("442")) {
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] s = parser.getMiddle().trim().split(" ");
            String[] msg = {null, "*** You are not on " + s[1]};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("443")) //user already in channel invited to
        {
            String[] p = parser.getParams().trim().split(" ");
            String invited = p[1], channelName = p[2];
            String[] msg = {null, "*** " + invited + " is already in channel " + channelName + "."};
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("446")) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("451")) //you have not registered
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("461")) //not enough parameters  (/stats rieux)
        {
            String[] s = parser.getParams().split(" ");
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, s[1] + ": " + parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("462")) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.serverStyle, false);
            return;
        }
        if (command.equals("467") || command.equals("468")) // No such server / too many targets 
        {                                                   //PLACEHOLDERS
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, "*** " + parser.getMiddle().trim() + ": " + parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("471") || command.equals("472")) // channel is full / unknown mode
        {                                                   // PLACEHOLDER
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, "*** " + parser.getMiddle().trim() + ": " + parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("473") || command.equals("474") || command.equals("475")) //cannot join channel
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            if (channel != null) channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("477")) //need to identify to a registered nick to join channel
        {
            String chan = parser.getMiddle().substring(parser.getMiddle().indexOf(" ")+1);
            String[] msg = {null, chan+": "+parser.getTrailing()};
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("478")) // ban list full
        {                          // PLACEHOLDER
            ChannelPanel channel = ((ChannelPanel) tabbedPane.getSelectedComponent());
            String[] msg = {null, "*** " + parser.getMiddle().trim() + ": " + parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("479")) //illegal channel name
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("481")) //permission denied (/stats rieux)
        {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("482")) //chan ops required
        {                           //PLACEHOLDER
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getMiddle() + ": " + parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("483")) //can't kill server
        {                           //placeholder
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("484")) //Cannot kill, kick or deop channel service 
        {                           //PLACEHOLDER
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getMiddle() + ": " + parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("491")) //No O-lines for your host
        {                           //PLACEHOLDER
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("501") || command.equals("502") || command.equals("510")) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, GUI.errorStyle, false);
            return;
        }
        if (command.equals("671")) //whois: using secure connection
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            String[] msg = {null, target + " " + info};
            channel.insertString(msg, GUI.serverStyle, false);
        } else {
            System.out.println(line);
        }
    }




    /** Closes socket */
    public void disconnect() {
        try {
            this.socket.close();
            this.isConnected = false;
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            //do nothing
        }
    }




    /** opens socket and sends initial NICK and USER info to the server */
    @Override
    public void run() {
        try {
            socket = new Socket(server, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String line;
            while (socket.isConnected()) {
                isConnected = true;
                send("NICK " + nicks[0]);
                send("USER " + nicks[0] + "123" + " 8 * : " + real);
                while ((line = reader.readLine()) != null) {
                    parseFromServer(line);
                }

            }
        } catch (IOException ex) {
            //do nothing
        } catch (BadLocationException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}