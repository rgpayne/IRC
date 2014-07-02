import java.awt.*;
import javax.swing.text.BadLocationException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.*;
import org.apache.commons.lang3.StringUtils;


public class Connection implements Runnable{
    
    public static final String CTCP_DELIM = "\001", CTCP_BOLD_DELIM = "\002",CTCP_COLOR_DELIM = "\003",
                               CTCP_UNDERLINE_DELIM = "\037", CTCP_RESET_DELIM = "\017"; //reset -> 0x1F
    Thread thread;
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    String server, password, title;
    boolean autoconnect = true;
    static String[] nicks = {"", "", ""};
    static String currentNick = "", real = "", awayMessage="";
    int port;
    static JTabbedPane tabbedPane;
    static JLabel tabInfo;
    

    public Connection(String title, String server, int port)
    { 
       this.title = title;
       this.server = server;
       this.port = port;
       
       thread = new Thread(this);
       thread.start();
    }
    public Connection(String title, String server, int port, boolean autoconnect)
    {
        this.title = title;
        this.server = server;
        this.port = port;
        this.autoconnect = autoconnect;
        thread = new Thread(this);
        thread.start();
    }
    public void send(String line) throws IOException, BadLocationException
    {
        if (line.toUpperCase().equals("QUIT")){
            this.writer.write("quit+\r\n");
            disconnect();
            return;
        }
        this.writer.write(line+"\r\n");
        this.writer.flush();   
       
    }
    public static int findTab(String title, Connection conn)
    {
        int totalTabs = tabbedPane.getTabCount();
        for (int i = 0; i < totalTabs; i++){
            ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(i);
            String channelName = channel.name;
            Connection c = channel.connection;
            if (channelName.equalsIgnoreCase(title) && conn == c) return i;
        }
        return -1;
    }
    public boolean checkForCTCPDelims(String line)
    {
        if (line.contains(CTCP_BOLD_DELIM) || line.contains(CTCP_COLOR_DELIM) || line.contains(CTCP_DELIM) || line.contains(CTCP_UNDERLINE_DELIM)) return true;
        return false;
    }

    public void parseFromServer(String line) throws IOException, BadLocationException
    {
        final Parser parser = new Parser(line);
        String command = parser.getCommand();
        //System.out.println(line);
        //System.out.println(parser.toString());
        if (command.equals("AWAY"))
        {
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab("#" + channelName, this);
            String[] msg = {null, parser.getTrailing()};
            ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(indexOfChannel);
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        }
        if (command.equals("JOIN"))
        {
           if (currentNick.equals(parser.getNick())) //if joined is me
           {
               String channelName = parser.getTrailing();
               if (channelName.equals("")){
                   channelName = parser.getParams().trim();
               }
               if (channelName.startsWith("#"))
               {
                   int indexOfChannel = findTab(channelName, this);
                   if (indexOfChannel == -1)
                   {
                       
                       
                     final String channelName2 = channelName;

                       try {
                           SwingUtilities.invokeAndWait(new Runnable() {
                               
                               @Override
                               public void run() {
                                   
                                   try {
                                       ChannelPanel channel = new ChannelPanel(channelName2, channelName2, currentNick, Connection.this);
                                       String[] msg = {null, "You ("+parser.getPrefix()+") have joined "+parser.getParams().trim().substring(1)+"."};
                                       channel.insertString(msg, ChannelPanel.connectStyle, false);
                                       int newTabIndex = findTab(channel.name, Connection.this);
                                       tabbedPane.setSelectedIndex(newTabIndex);
                                   } catch (BadLocationException | IOException ex) {
                                       Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                                   }
                                   
                               }
                           });
                           
                           
                           
                           
                           return;
                       } catch (InterruptedException ex) {
                           Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                       } catch (InvocationTargetException ex) {
                           Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                       }
                   }
                   else{ //joining a room for which you already have a tab (i.e. you were kicked or lost connection or something)
                       
                       Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                       ChannelPanel channel = ((ChannelPanel)aComponent);
                       String[] msg = {null, "Joined "+channelName};
                       channel.insertString(msg ,ChannelPanel.serverStyle, false);
                   }
               }
           }
           else //if joined isn't me
           {
               String channelName = parser.getTrailing();
               if (channelName.startsWith("#"))
               {
                   int indexOfChannel = findTab(channelName, this);
                   if (indexOfChannel != -1)
                   {
                       Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                       ChannelPanel channel = ((ChannelPanel)aComponent);
                       channel.addToUserList(parser.getNick());
                       String[] msg = {null, "--> "+parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() +  ") has joined the channel."};
                       channel.insertString(msg, ChannelPanel.joinStyle, false);
                       return;                       
                   }
               }
           }
        }
        if (command.equals("KICK"))
        {
            String middle = parser.getMiddle();
            String channelName = middle.substring(0,middle.indexOf(" "));
            String kicked = middle.substring(middle.indexOf(" ")).trim();
            String kickedBy = parser.getNick();
            String kickMessage = parser.getTrailing();
            
            int indexOfChannel = findTab(channelName, this);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = (ChannelPanel)aComponent;
                        
            if (kicked.equals(currentNick)) //i am kicked
            {
               String[] msg =  {null, "*** You have been kicked from the channel by "+kickedBy+ " ("+kickMessage+")"};
               channel.insertString(msg, ChannelPanel.serverStyle, false);
               channel.clear();
               return;
            }
            if (kickedBy.equals(currentNick)) //i kick somebody
            {
                String[] msg = {null, "*** You have kicked "+kicked+" from the channel ("+kickMessage+")"};
                channel.insertString(msg, ChannelPanel.serverStyle, false);
                channel.removeFromUserList(kicked);
                return;
            }
            else //somebody else kicked
            {
                String[] msg = {null, "*** "+kicked+" was kicked from the channel ("+kickMessage+")"};
                channel.insertString(msg, ChannelPanel.disconnectStyle, false);
                channel.removeFromUserList(kicked);
                return;
            }
        }
        if (command.equals("MODE"))
        {
            if (parser.getServer().equals(currentNick) || parser.getPrefix().equals(currentNick)) //setting personal mode
            {
                Component aComponent = tabbedPane.getSelectedComponent();
                ChannelPanel channel = ((ChannelPanel)aComponent);
                String[] msg = {null, "[Mode] You have set personal modes: "+parser.getTrailing()};
                channel.insertString(msg, ChannelPanel.serverStyle, false);
                return;
            }    
            
            else //setting a user's mode
            { 
                String[] s = parser.getParams().trim().split(" ");
                String receiver = "";
                String giver = parser.getNick();
                String chan = s[0];
                String power = s[1];
                if (s.length >= 3) receiver = s[2];

                int indexOfChannel = findTab(chan, this);
                Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                ChannelPanel channel = ((ChannelPanel)aComponent);

                if (!receiver.equals(""))
                {
                String[] msg = {null, "*** "+giver+" "+power+" "+receiver};
                channel.insertString(msg, ChannelPanel.serverStyle, false);
                channel.removeFromUserList(receiver);

                String newNick = receiver;
                if (power.equals("+o")) newNick = "@"+receiver; //operator
                if (power.equals("+v")) newNick = "+"+receiver; //voice
                if (power.equals("+a")) newNick = "&"+receiver; //admin
                if (power.equals("+h")) newNick = "%"+receiver; //half-op
                if (power.equals("+q")) newNick = "~"+receiver; //owner
                if (newNick.equals(receiver)) newNick = " "+receiver;
                channel.addToUserList(newNick);
                return;
                }
                if (giver.equals(""))
                {
                    String[] ss = {null, "*** "+"Channel mode set to: "+power};
                    channel.insertString(ss, ChannelPanel.serverStyle, false); //channel mode set by nobody
                    return;
                }
                else
                {
                    String[] ss = {null, "*** "+giver+" set the channel to: "+power};
                    channel.insertString(ss, ChannelPanel.serverStyle, false); //channel mode
                    return;
                }
            }
        }
        if (command.equals("NICK"))
        {
            String prefix = parser.getNick().substring(0,1);
            String oldNick = parser.getNick();
            String newNick = parser.getTrailing();
            
            if (!currentNick.equals(oldNick)) //if someone else changes name
            {
                for (int i = 0; i < tabbedPane.getTabCount(); i++)
                {
                    Component aComponent = tabbedPane.getComponentAt(i);
                    ChannelPanel channel = ((ChannelPanel)aComponent);
                    
                    if (channel.contains(" "+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList(" "+newNick);
                        String[] msg = {null, "*** "+ oldNick+" is now known as "+newNick+"."};
                        channel.insertString(msg, ChannelPanel.serverStyle, false);
                        return;
                    }
                    if (channel.contains("@"+oldNick))
                    {
                        String[] msg = {null, "*** "+ oldNick+" is now known as "+newNick+"."};
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("@"+newNick);
                        channel.insertString(msg, ChannelPanel.serverStyle, false);
                        return;
                    }
                    if (channel.contains("~"+oldNick))
                    {
                        String[] msg = {null, "*** "+oldNick+" is now known as "+newNick+"."};
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("~"+newNick);
                        channel.insertString(msg, ChannelPanel.serverStyle, false);
                        return;
                    }
                    if (channel.contains("+"+oldNick))
                    {
                        String[] msg = {null, "*** "+ oldNick+" is now known as "+newNick+"."};
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("+"+newNick);
                        channel.insertString(msg, ChannelPanel.serverStyle, false);        
                        return;
                    }
                    if (channel.contains("%"+oldNick))
                    {
                        String[] msg = {null, "*** "+ oldNick+" is now known as "+newNick+"."};
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("%"+newNick);
                        channel.insertString(msg, ChannelPanel.serverStyle, false);  
                        return;
                    }
                    if (channel.contains("&"+oldNick))
                    {
                        String[] msg = {null, "*** "+ oldNick+" is now known as "+newNick+"."};
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("&"+newNick);
                        channel.insertString(msg, ChannelPanel.serverStyle, false);   
                        return;
                    }
                }
                return;
            }
            else //if you change your name
            {
                for (int i = 0; i < tabbedPane.getTabCount(); i++)
                {
                    String[] msg = {null, "*** You are now known as "+newNick};
                    Component aComponent = tabbedPane.getComponentAt(i);
                    ChannelPanel channel = ((ChannelPanel)aComponent);
                    channel.removeFromUserList(oldNick);
                    this.currentNick = prefix+newNick.substring(1);
                    channel.addToUserList(this.currentNick);
                    channel.insertString(msg, ChannelPanel.serverStyle, false);
                }
                return;
            }
        }
        if (command.equals("NOTICE")) 
        {
            boolean ctcp = checkForCTCPDelims(line);
            String h = parser.getPrefix();
            int indexOfChannel = findTab(h, this);
            ChannelPanel channel;
            
            if (indexOfChannel == -1) channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            else channel = (ChannelPanel)tabbedPane.getComponentAt(indexOfChannel);
            
            if (channel == null){
                return; //this is shit
            }
            
            String nick = parser.getNick();
            
            if (!nick.equals("")){
                String[] msg = {null, "[Notice] -"+nick+"- "+parser.getTrailing()};
                channel.insertString(msg, ChannelPanel.connectStyle, ctcp);
            }
            else{
                String[] msg = {null, "[Notice] "+parser.getTrailing()};
                channel.insertString(msg, ChannelPanel.connectStyle, ctcp);
                if (!h.equals("")) channel.server = parser.getPrefix();

            }

            return;
        }
        if (command.equals("PART"))
        {
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);
            
            int indexOfChannel = findTab(channelName, this);
            
            if (currentNick.equals(parser.getNick())){ //if i'm leaving
                tabbedPane.remove(indexOfChannel);
                return;
            }
            else
            {
                Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                String[] msg = {null, "<-- "+parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has left the channel (" + parser.getTrailing()+ ")" };
                ((ChannelPanel)aComponent).insertString(msg, ChannelPanel.disconnectStyle, false);
                ((ChannelPanel)aComponent).removeFromUserList(parser.getNick());
                return;
            }
        }
        if (command.equals("PING"))
        {
            send("PONG :"+ parser.getTrailing());
            return;
        }
        if (command.equals("PRIVMSG") || command.equals("MSG"))
        {
            final boolean ctcp = checkForCTCPDelims(line);
            if (parser.getTrailing().startsWith(CTCP_DELIM))
            {
                if (parser.getTrailing().substring(1).startsWith("ACTION")) //CTCP action
                {
                    String channelName = parser.getMiddle();
                    int indexOfChannel = findTab(channelName, this);
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(indexOfChannel);
                    String rest = parser.getTrailing().substring(7);
                    String[] s = {parser.getNick(), rest};
                    channel.insertCTCPAction(s);
                    return;                    
                }
                if (parser.getTrailing().substring(1).startsWith("CLIENTINFO")) //CTCP clientinfo
                {
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                    String[] msg = {null, "[CTCP] "+"Received CTCP-ClientInfo request from "+parser.getNick()+"."};
                    channel.insertString(msg, ChannelPanel.connectStyle, false);
                    this.send("NOTICE "+parser.getNick()+" :\001CLIENTINFO CTCP commands: ACTION FINGER PING SOURCE TIME USERINFO VERSION\001");
                    return;  
                }
                if (parser.getTrailing().substring(1).startsWith("FINGER")) //CTCP Finger
                {
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                    String[] msg = {null, "[CTCP] "+"Received CTCP-Finger request from "+parser.getNick()+"."};
                    channel.insertString(msg, ChannelPanel.connectStyle, false);
                    this.send("NOTICE "+parser.getNick()+" :\001FINGER "+ChannelPanel.CTCPFingerMessage+"\001");
                    return;
                }
                if (parser.getTrailing().substring(1).startsWith("PING")) //CTCP Ping
                {
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                    String[] msg = {null, "[CTCP] "+"Recieved CTCP-Ping request from "+parser.getNick()+"."};
                    channel.insertString(msg, ChannelPanel.connectStyle, false);
                    this.send("NOTICE "+parser.getNick()+" :"+parser.getTrailing());
                    return;
                }
                if (parser.getTrailing().substring(1).startsWith("SOURCE")) //CTCP source
                {
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                    String[] msg = {null, "[CTCP] "+"Received CTCP-Source request from "+parser.getNick()+"."};
                    channel.insertString(msg, ChannelPanel.connectStyle, false);
                    this.send("NOTICE "+parser.getNick()+" :\001SOURCE Unavailable\001");
                    return;                    
                }
                if (parser.getTrailing().substring(1).startsWith("TIME")) //CTCP Time
                {
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                    String[] msg = {null, "[CTCP] "+"Received CTCP-Time request from "+parser.getNick()+"."};
                    channel.insertString(msg, ChannelPanel.connectStyle, false);
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm aa");
                    sdf.setTimeZone(TimeZone.getTimeZone("CST"));
                    String time = sdf.format(date);  
                    this.send("NOTICE "+parser.getNick()+" :\001TIME "+time+"\001");
                    return;
                }
                if (parser.getTrailing().substring(1).startsWith("USERINFO")) //ctcp userinfo
                {
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                    String[] msg = {null, "[CTCP] "+"Received CTCP-UserInfo request from "+parser.getNick()+"."};
                    channel.insertString(msg, ChannelPanel.connectStyle, false);
                    this.send("NOTICE "+parser.getNick()+" :\001USERINFO "+ChannelPanel.CTCPUserInfo+"\001");
                    return;    
                }
                if (parser.getTrailing().substring(1).startsWith("VERSION")) //CTCP Version
                {
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                    String[] msg = {null, "[CTCP] "+"Received CTCP-Version request from "+parser.getNick()+"."};
                    channel.insertString(msg, ChannelPanel.connectStyle, false);
                    this.send("NOTICE "+parser.getNick()+" :\001VERSION AlphaClient:v0.1:LM17\001");  //placeholder
                    return;
                }
                else
                {
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                    String[] msg = {null, "[CTCP] "+"Received invalid CTCP request from "+parser.getNick()+": "+parser.getTrailing().substring(1)};
                    channel.insertString(msg, ChannelPanel.connectStyle, false);
                    return;
                }
            }
            
            //STANDARD PRIV MESSAGE
            String channelName = parser.getMiddle();
            if (channelName.equals(currentNick))
            {
                channelName = parser.getNick();
                final String channelName2 = channelName;
                int indexOfChannel = findTab(channelName, this);
                if (indexOfChannel == -1)
                {
                    
                    
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            
                            @Override
                            public void run() {
                                
                                ChannelPanel channel = null;
                                try {
                                    channel = new ChannelPanel(channelName2, channelName2, currentNick, Connection.this);
                                } catch (BadLocationException | IOException ex) {
                                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                channel.setRightComponent(null);
                                channel.setDividerSize(0);
                                String[] msg = {channelName2, parser.getTrailing()};
                                try {
                                    channel.insertString(msg, ChannelPanel.chatStyle, ctcp);
                                } catch (BadLocationException | IOException ex) {
                                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                        
                        
                        
                        return;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                String[] msg = {parser.getNick(), parser.getTrailing().trim()};
                ((ChannelPanel)aComponent).insertString(msg, ChannelPanel.chatStyle, ctcp);
                return;
            }
            int indexOfChannel = findTab(channelName, this);
            if (indexOfChannel == -1)
            {
                final String channelName2 = channelName;
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        
                        @Override
                        public void run() {
                            
                            try {
                                ChannelPanel channel = new ChannelPanel(channelName2, channelName2, currentNick, Connection.this);
                                                                        String[] msg = {channelName2, parser.getTrailing()};
                channel.insertString(msg, ChannelPanel.chatStyle, ctcp);  
                            } catch (BadLocationException ex) {
                                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                        }

                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
                              
                return;
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            String[] msg = {parser.getNick().trim(), parser.getTrailing().trim()};
            ((ChannelPanel)aComponent).insertString(msg, ChannelPanel.chatStyle, ctcp);   
            return;
        }
        if (command.equals("QUIT"))
        {   
            boolean ctcp = checkForCTCPDelims(line);
            String quitter = parser.getNick().trim();
            String quitMessage = parser.getParams().substring(2); //fix so it only quits on connection user quit on?
            for (int i = 0; i < tabbedPane.getTabCount(); i++){
                Component aComponent = tabbedPane.getComponentAt(i);
                ChannelPanel channel = ((ChannelPanel)aComponent);
                boolean success = channel.removeFromUserList(quitter);
                String[] msg = {null, "<-- "+quitter+" has left the server ("+quitMessage+")"};
                if (success){
                    channel.insertString(msg, ChannelPanel.disconnectStyle, ctcp);
                    
                }
            }
            return;            
        }
        if (command.equals("TOPIC"))
        {
            boolean ctcp = checkForCTCPDelims(line);
            String channelName = parser.getMiddle();
            int indexOfChannel = findTab(channelName, this);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.topic = parser.getTrailing();
            
            if (currentNick.equals(parser.getNick()))
            {
                String[] msg = {null, "*** You set the channel topic to: "+ channel.topic};
                channel.insertString(msg, ChannelPanel.serverStyle, ctcp);
            }
            else
            {
                String[] msg = {null, parser.getNick()+" has changed the topic to: "+channel.topic};
                channel.insertString(msg, ChannelPanel.serverStyle, ctcp);
            }
            return;
            
        }
        if (command.equals("WHOIS"))
        {
         //does this even exist?   
        }
        if (command.equals("001") || command.equals("002") || command.equals("003") || command.equals("004") || command.equals("005"))
        {
            boolean ctcp = checkForCTCPDelims(line);
            final String host = parser.getPrefix();
            int indexOfChannel = findTab(host, this);
            if (indexOfChannel == -1)
            {
                
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        
                        @Override
                        public void run() {
                            
                            try {
                                ChannelPanel c = new ChannelPanel(Connection.this.title, host, currentNick, Connection.this);
                                c.server = parser.getPrefix();
                            } catch (BadLocationException ex) {
                                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
             indexOfChannel = findTab(host, this);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String text = parser.getTrailing();
            if (text.equals("")) text = parser.getParams().trim().substring(currentNick.length()).trim();
            String[] msg = {null,"[Welcome] "+text};
            channel.insertString(msg, ChannelPanel.connectStyle, ctcp);
            
            if (command.equals("001" ) && this.autoconnect )
            {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (int i = 0; i < GUI.savedConnections.size(); i++)
                {
                    SavedConnection conn = GUI.savedConnections.get(i);
                    if (conn.getServer().equals(channel.connection.server))
                    {
                        ArrayList<String> c = conn.retrieveChannels();
                        for(int j = 0; j < c.size(); j++)
                        {
                            send("JOIN "+c.get(j));
                        }
                    }
                }  
            }  
            
            
            
            return;
        }
        if (command.equals("042")) //unique id
        {
            int index = findTab(parser.getPrefix(), this);
            ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(index);
            String[] msg = {null, "[042] "+parser.getParams().trim()+"."};
            channel.insertString(msg, ChannelPanel.connectStyle, false);
            return;
        }
        if (command.equals("219")) //end of /STATS
        {
            return;
        }
        if (command.equals("221")) //requesting to see own modes (/modes rieux)
        {
            return;
        }
        if (command.equals("242")) //uptime
        {
            boolean ctcp = checkForCTCPDelims(line);
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, ChannelPanel.serverStyle, ctcp);
            return;
        }
        if (command.equals("251") || command.equals("255"))
        {
            boolean ctcp = checkForCTCPDelims(line);
            String p = parser.getParams();
            String message = p.substring(p.indexOf(":")+1);
            
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host, this);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Users] "+message+"."};
            channel.insertString(msg, ChannelPanel.connectStyle, ctcp);
            return;
        }
        if (command.equals("252") || command.equals("253") || command.equals("254"))
        {
            boolean ctcp = checkForCTCPDelims(line);
            String[] s = parser.getParams().trim().split(" ");
            String digit = s[1];
            String msg = parser.getParams().substring(parser.getParams().indexOf(":")+1);
            
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host, this);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] input = {null, "[Users] "+digit+" "+msg+"."};
            channel.insertString(input, ChannelPanel.connectStyle, ctcp);
            return;
        }
	if (command.equals("256") || command.equals("257") || command.equals("258") || command.equals("259")) //placeholders
        {
            boolean ctcp = checkForCTCPDelims(line);
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host, this);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Users] "+parser.getTrailing()};
            channel.insertString(msg, ChannelPanel.connectStyle, ctcp);
            return;
        }
        if (command.equals("263")) //server load too heavy. please try again (happens with /list)
        {
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        }
        if (command.equals("265") || command.equals("266"))
        {
            boolean ctcp = checkForCTCPDelims(line);
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host, this);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Users] "+parser.getTrailing()};
            channel.insertString(msg, ChannelPanel.connectStyle, ctcp);
            return;
        }
        if (command.equals("301")) //received when target of whois, privmsg, etc is set to away
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {"[Whois] "+target+" is away: "+info};
            channel.insertString(msg, ChannelPanel.serverStyle, false);             
        }
        if (command.equals("305")) //no longer away
        {
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            String[] msg = {null, "[Away] You are no longer marked as away."};
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        }
        if (command.equals("306")) //away
        {
            boolean ctcp = checkForCTCPDelims(line);
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            String[] msg = {null, "[Away] You have been marked as away (Reason: "+ChannelPanel.awayMessage+")"};
            channel.insertString(msg, ChannelPanel.serverStyle, ctcp);
            return;            
        }
        if (command.equals("307"))
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Whois] "+target+" "+info};
            channel.insertString(msg, ChannelPanel.serverStyle, false); 
        }
        if (command.equals("310"))
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String modes = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Whois] "+target+" "+modes};
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        }
        if (command.equals("311")) //Whois user
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String fulladd = s[2]+"@"+s[3]+" ("+parser.getTrailing().trim()+")";
            
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Whois] "+target+" is "+fulladd};
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        
        }
        if (command.equals("312")) //whois server
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            String srv = parser.getPrefix();
            
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Whois] "+target+" is online via "+srv+" ("+info+")"};
            channel.insertString(msg, ChannelPanel.serverStyle, false);        
            return;
            
        }
        if (command.equals("313")) //whois operator
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Whois] "+target+" is an IRC Operator"};
            channel.insertString(msg, ChannelPanel.serverStyle, false);        
            return;
        }
        if (command.equals("314"))
        {
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        }
        if (command.equals("315")) //end of /who
        {
            String s = parser.getMiddle().split(" ")[1];
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            String[] msg = {null, "[Who] End of /WHO list for "+s};
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        }
        if (command.equals("317")) //whois idletime
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String idleTime = s[2];
            String time = s[3];

            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            
            Date date = new Date(Integer.valueOf(time) * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm aa z");
            sdf.setTimeZone(TimeZone.getTimeZone("CST"));
            channel.signOnTime = sdf.format(date);
            String[] msg1 = {null, "[Whois] "+target+" has been idle for "+idleTime+" seconds"};
            String[] msg2 = {null, "[Whois] "+target+" has been online since "+channel.signOnTime};
            channel.insertString(msg1, ChannelPanel.serverStyle, false);
            channel.insertString(msg2, ChannelPanel.serverStyle, false);
            
        }
        if (command.equals("318")) //end of whois
        {
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Whois] "+parser.getTrailing()};
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        }
        if (command.equals("319")) //whois channels
        {
            String[] s = parser.getParams().split(" ");  
            String target = s[2];
            String chans = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Whois] "+target+" is a user on channels: "+chans};
            channel.insertString(msg, ChannelPanel.serverStyle, false); 
            return;
            
        }
        if (command.equals("331")) //no topic
        {
            //channel.server = parser.getPrefix();
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
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.topic = parser.getTrailing();
            channel.server = parser.getPrefix();
            String[] msg = {null, "Current Topic: "+channel.topic};
            channel.insertString(msg, ChannelPanel.serverStyle, ctcp);
            return;
        }
        if (command.equals("333")) //time of topic change and who set it
        {
            String[] tokens = new String[5];
            int i = 0;
            StringTokenizer st = new StringTokenizer(parser.getParams()," #!");
            while (st.hasMoreTokens())
            {
                String str = st.nextToken();
                tokens[i] = str;
                i++;
            }
            
            String channelName = "#"+tokens[1];           
            int indexOfChannel = findTab(channelName, this);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.topicAuthor = tokens[tokens.length-3];
            //channel.signOnTime = tokens[tokens.length-2];
            
            //System.out.println(channel.topicAuthor+"___"+channel.time);
            
            //Date date = new Date(Integer.valueOf(channel.time) * 1000L);
            //SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm aa z");
            //sdf.setTimeZone(TimeZone.getTimeZone("CST"));
            //channel.signOnTime = sdf.format(date);
            return;
        }
        if (command.equals("338")) //whois actually
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Whois] "+target+" "+info};
            channel.insertString(msg, ChannelPanel.serverStyle, false);    
            return;                    
        }
        if (command.equals("352")) //who reply
        {
            String[] s = parser.getMiddle().split(" ");
            String person;
            if (s.length >= 5) person = s[5];
            else 
            {
                String[] p = parser.getParams().split(" ");
                person = p[5];
            }
            String[] msg = {null, "[Who] "+person+" is "+s[2]+"@"+s[3]+" ("+parser.getTrailing().substring(2)+")"};
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            
        }
        if (command.equals("353")) //names command
        {
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);
            int indexOfChannel = findTab(channelName, this);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            String[] nn = parser.getTrailing().split(" ");
            for (int i = 0; i < nn.length; i++)
            {
                String name = nn[i];
                char first = name.charAt(0);
                if (first == '@' || first == '+' || first == '%' || first == '~' || first == '&') continue;
                else nn[i] = " "+name;
            }
            ((ChannelPanel)aComponent).list.addAll(Arrays.asList(nn));
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
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel); 
            ChannelPanel channel = ((ChannelPanel)aComponent);
            
            Iterator<String> iterator = channel.list.iterator();
            while (iterator.hasNext())
            {
                String nextElement = iterator.next();
                channel.addManyToUserList(nextElement);
            }
            channel.list.clear();
            channel.server = parser.getPrefix();
            channel.fireIntervalAdded();
            return;
            }
        if (command.equals("369")) //end of whowas
        {
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            String [] msg = {null, parser.getTrailing()};
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        }
        if (command.equals("371") || command.equals("372") || command.equals("374") || command.equals("375") || command.equals("376"))
        {
            boolean ctcp = checkForCTCPDelims(line);
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host, this);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            if (command.equals("372")) channel.server = host;
            String[] msg = {null, "[MOTD] "+parser.getTrailing()};
            channel.insertString(msg, ChannelPanel.connectStyle, ctcp);
            return;
        }
        if (command.equals("401")) //no such nick/channel
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            String[] msg = {null, "[Error] "+s[1]+": No such nick/channel."};
            channel.insertString(msg, ChannelPanel.errorStyle, false);
            return;            
        }
        if (command.equals("402")) //no such server
        {            
        }
        if (command.equals("403")) //no such channel 
        {
            String[] s = parser.getParams().split(" ");
            String chan = s[1].trim();
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            String[] msg = {null, "[Error] "+chan+": No such channel. "};
            channel.insertString(msg, ChannelPanel.errorStyle, false);
        }
        if (command.equals("404"))
        {
            String[] s = parser.getParams().split(" ");
            String chan = s[1].trim();
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            String[] msg = {null, "[Error] "+chan+": Cannot send to channel."};
            channel.insertString(msg, ChannelPanel.errorStyle, false);
            return;          
        }
        if (command.equals("405")) //joined too many channels
        {
            String[] s = parser.getParams().trim().split(" ");
            String chan = s[1].trim();
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            String[] msg = {null, "[Error] "+chan+": You have joined too many channels."};
            channel.insertString(msg, ChannelPanel.errorStyle, false);
            return;
        }
        if (command.equals("406")) //there was no such nickname (whowas)
        {
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            String[] msg = {null, parser.getTrailing()};
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        }
        if (command.equals("412")) //no text to send
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            String[] msg = {null, "[Error] "+s[0]+" no text to send."}; 
            channel.insertString(msg, ChannelPanel.errorStyle, false);
            return;
        }
        if (command.equals("421")) //unknown command
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            String[] msg = {null, "[Error] "+s[1]+": Unknown command."};
            channel.insertString(msg, ChannelPanel.errorStyle, false);
            return;
        }
        if (command.equals("432")) //eroneous nickname
        {
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            String[] msg = {null, "[Nick] Erroneous Nickname"};
            channel.insertString(msg, ChannelPanel.errorStyle, false);
            return;
        }        
        if (command.equals("433")) //nick in use
        {
            String[] s = parser.getParams().trim().split(" ");
            String newNick = "";
            for (int i = 0; i < nicks.length; i++)
            {
                if (s[1].equals(nicks[i]))
                {
                    if (i < 2) newNick = nicks[i+1];
                }
            }
            this.currentNick = newNick;
            if (currentNick.equals(""))
            {
                ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
                String[] msg = {null, "[Error] All your nicks are taken"};
                channel.insertString(msg, ChannelPanel.errorStyle, false);
                channel.connection.send("quit");
                return;
            }
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            String[] msg = {null, "[Error] **"+parser.getParams().trim()};
            channel.insertString(msg, ChannelPanel.errorStyle, false);
            channel.connection.send("NICK "+currentNick);
            return;       
        }
        if (command.equals("437")) //cannot change nickname while banned or moderated on channel
        {
        }
        if (command.equals("439")) //please wait until we process your connection
        {
            int indexOfChannel = tabbedPane.getSelectedIndex();
            if (indexOfChannel == -1)
            {
                
                
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        
                        @Override
                        public void run() {
                            
                            try {
                                new ChannelPanel(title, parser.getPrefix(), Connection.this.currentNick, Connection.this);
                            } catch (BadLocationException | IOException ex) {
                                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                        }
                    });
                    indexOfChannel = findTab(parser.getPrefix(), this);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }

                
            }
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getComponentAt(indexOfChannel));
            channel.server = parser.getPrefix();
            String[] msg = {null, "Please wait while we process your connection..."};
            channel.insertString(msg, ChannelPanel.connectStyle, false);
            return;            
        }
        if (command.equals("443")) //user already in channel invited to
        {
            return;
        }
        if (command.equals("451")) //you have not registered
        {
            return;
        }
        if (command.equals("461")) //not enough parameters  (/stats rieux)
        {
            String[] s = parser.getParams().split(" ");
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            String[] msg = {null, s[1]+": "+parser.getTrailing()};
            channel.insertString(msg, ChannelPanel.serverStyle, false);
            return;
        }
        if (command.equals("481")) //permission denied (/stats rieux)
        {
            return;
        }
        if (command.equals("671")) //whois: using secure connection
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            String[] msg = {null, "[Whois] "+target+" "+info};
            channel.insertString(msg, ChannelPanel.serverStyle, false);    
            return;
        }
        else
        {
            System.out.println(line);
            //Component aComponent = tabbedPane.getComponentAt(0);
            //((ChannelPanel)aComponent).insertString(parser.getCommand(), ChannelPanel.serverStyle);
        }        
    }
    public void disconnect()
    {
        try {
            this.socket.close();
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            //do nothing
        }
    }
    
    public void run()
    {
        try 
        {
            socket = new Socket(server, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String line;
            while (socket.isConnected())
            {
                send("NICK "+nicks[0]);
                send("USER "+nicks[0]+"123"+" 8 * : "+real);
                while ((line = reader.readLine()) != null)
                {                   
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