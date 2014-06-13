import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.BadLocationException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;


public class Connection implements Runnable{
    Thread thread;
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    FileWriter chatLog;
    String server, host, password;
    static String nick = "rieux";
    int port;
    DefaultStyledDocument doc, userList;
    static JTabbedPane tabbedPane;
    JLabel tabInfo;
    ChannelPanel first;
    

    public Connection(String server, int port, DefaultStyledDocument doc, DefaultStyledDocument userList, JTabbedPane tabbedPane, JLabel tabInfo)
    {
       this.server = server;
       this.port = port;
       this.doc = doc;
       this.userList = userList;
       this.password = password;
       
       this.tabbedPane = tabbedPane;
       this.tabInfo = tabInfo;
       
       //first = new ChannelPanel(server, nick);
       //tabbedPane.add("Name", first);
       
       thread = new Thread(this);
       thread.start();
    }     
    public void send(String line) throws IOException, BadLocationException
    {
        if (line.toUpperCase().equals("QUIT")){
            System.exit(0);
            return;
        }
        this.writer.write(line+"\r\n");
        this.writer.flush();   
       
    }
    public static String formatNickname(String nickname)
    {
        int formatlen = 12;
        String blank = "";
        int len = nickname.length();
        if (len >= formatlen) return nickname;
        else{
            for (int i = 0; i < (formatlen - len); i++){
                blank = blank+" ";
            }
            return blank + nickname;
        }
    }
    public static int findTab(JTabbedPane tabbedPane, String title)
    {
        int totalTabs = tabbedPane.getTabCount();
        for (int i = 0; i < totalTabs; i++){
            String tabTitle = tabbedPane.getTitleAt(i);
            if (tabTitle.equalsIgnoreCase(title)) return i;
        }
        return -1;
    }

    public void parseFromServer(String line) throws IOException, BadLocationException
    {
        System.out.println(line);
        Parser parser = new Parser(line);
        String command = parser.getCommand();
        
        if (command.equals("AWAY"))
        {
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(0);
                channel.insertString("___"+line, "doc");
            } 
            ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(indexOfChannel);
            channel.insertString(parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("JOIN"))
        {
           if (nick.equals(parser.getNick())) //if joined is me
           {
               String channelName = parser.getTrailing();
               if (channelName.startsWith("#"))
               {
                   int indexOfChannel = findTab(tabbedPane, channelName);
                   if (indexOfChannel == -1)
                   {
                       ChannelPanel c = new ChannelPanel(channelName, nick, this);
                       int newTabIndex = findTab(tabbedPane, c.name);
                       tabbedPane.setSelectedIndex(newTabIndex);                     
                       return;
                   }
                   else
                   {
                        System.out.println("do we ever even get here tho?");
                        Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                        ((ChannelPanel)aComponent).insertString("*** "+parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() + "has joined the channel", "doc");   
                        return;
                   }
               }
           }
           else //if joined isn't me
           {
               String channelName = parser.getTrailing();
               if (channelName.startsWith("#"))
               {
                   int indexOfChannel = findTab(tabbedPane, channelName);
                   if (indexOfChannel != -1)
                   {
                       Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                       ChannelPanel channel = ((ChannelPanel)aComponent);
                       channel.addToUserList(parser.getNick());
                       channel.insertString("*** "+parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() +  ") has joined the channel.", "doc");
                      // channel.connection.send("WHO "+parser.getNick());
                       return;
                       
                   }
               }
           }
        }
        if (command.equals("KICK"))
        {
            //System.out.println(parser.getCommand() +" | "+parser.getHost()+" | "+parser.getMiddle()+" | " + parser.getNick()+" | "+parser.getParams()+" | "+parser.getPrefix()+ " | "+ parser.getServer()+" | "+parser.getTrailing()+" | "+parser.getUser());
            String middle = parser.getMiddle();
            String channelName = middle.substring(0,middle.indexOf(" "));
            String kicked = middle.substring(middle.indexOf(" ")).trim();
            String kickedBy = parser.getNick();
            String kickMessage = parser.getTrailing();
            
            int indexOfChannel = findTab(tabbedPane, channelName);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = (ChannelPanel)aComponent;
                        
            if (kicked.equals(nick)) //i am kicked
            {
               channel.insertString("*** You have been kicked from the channel by "+kickedBy+ " ("+kickMessage+")", "doc");
               channel.removeFromUserList(nick);
               channel.removeAllFromuserList();
               return;
            }
            if (kickedBy.equals(nick)) //i kick somebody
            {
                channel.insertString("*** You have kicked "+kicked+" from the channel ("+kickMessage+")", "doc");
                channel.removeFromUserList(kicked);
                return;
            }
            else //somebody else kicked
            {
                channel.insertString("*** "+kicked+" was kicked from the channel ("+kickMessage, server);
                channel.removeFromUserList(kicked);
                return;
            }
        }
        if (command.equals("MODE"))
        {
            //System.out.println(parser.getCommand() +" | "+parser.getHost()+" | "+parser.getMiddle()+" | " + parser.getNick()+" | "+parser.getParams()+" | "+parser.getPrefix()+ " | "+ parser.getServer()+" | "+parser.getTrailing()+" | "+parser.getUser());
            
            if (parser.getServer().equals(nick)) //setting personal mode
            {
                Component aComponent = tabbedPane.getSelectedComponent();
                ChannelPanel channel = ((ChannelPanel)aComponent);
                channel.insertString("[Mode] You have set personal modes: "+parser.getTrailing(), "doc");
            }
            else //setting channel mode
            { 
            String[] s = parser.getParams().trim().split(" ");
            String giver = parser.getNick();
            String chan = s[0];
            String power = s[1];
            String receiver = s[2];
            
            int indexOfChannel = findTab(tabbedPane, chan);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("*** "+giver+" "+power+" "+receiver, "doc");
            channel.removeFromUserList(receiver);
            
            String newNick = receiver;
            if (power.equals("+o")) newNick = "@"+receiver; //operator
            if (power.equals("+v")) newNick = "+"+receiver; //voice
            if (power.equals("+a")) newNick = "&"+receiver; //admin
            if (power.equals("+h")) newNick = "%"+receiver; //half-op
            if (power.equals("+q")) newNick = "~"+receiver; //owner
            if (newNick.equals(receiver)) System.out.println("______PROBLEM IN MODE_______");
            channel.addToUserList(newNick);
            }
            return;
        }
        if (command.equals("NICK"))
        {
            String oldNick = parser.getNick();
            String newNick = parser.getTrailing();
            System.out.println(parser.getCommand() +" | "+parser.getHost()+" | "+parser.getMiddle()+" | " + parser.getNick()+" | "+parser.getParams()+" | "+parser.getPrefix()+ " | "+ parser.getServer()+" | "+parser.getTrailing()+" | "+parser.getUser());
            System.out.println(nick+" "+oldNick+" "+newNick);
            
            if (!nick.equals(oldNick)) //if someone else changes name
            {
                for (int i = 0; i < tabbedPane.getTabCount(); i++)
                {
                    Component aComponent = tabbedPane.getComponentAt(i);
                    ChannelPanel channel = ((ChannelPanel)aComponent);
                    
                    if (channel.userSet.contains(" "+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList(" "+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc");
                        return;
                    }
                    if (channel.userSet.contains("@"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("@"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc");
                        return;
                    }
                    if (channel.userSet.contains("~"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("~"+newNick);
                        channel.insertString("*** "+oldNick+" is now known as "+newNick+".", "doc");
                        return;
                    }
                    if (channel.userSet.contains("+"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("+"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc");        
                        return;
                    }
                    if (channel.userSet.contains("%"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("%"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc");  
                        return;
                    }
                    if (channel.userSet.contains("&"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("&"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc");   
                        return;
                    }
                }
                return;
            }
            else //you change your name
            {
                for (int i = 0; i < tabbedPane.getTabCount(); i++)
                {
                    Component aComponent = tabbedPane.getComponentAt(i);
                    ChannelPanel channel = ((ChannelPanel)aComponent);
                    channel.removeFromUserList(oldNick);
                    channel.addToUserList(newNick);
                    channel.insertString("*** You are now known as "+newNick, "doc");
                    this.nick = newNick;
                }
            }
        }
        if (command.equals("MODE"))
        {
            //TODO
            return;
        }
        if (command.equals("NOTICE"))
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(tabbedPane, host);
            if (indexOfChannel == -1) return;
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString(parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("PART"))
        {
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);
            
            int indexOfChannel = findTab(tabbedPane, channelName);
            
            if (indexOfChannel == -1)
            {
                System.out.println("___/PART is broken___");
                return;
            }
            if (nick.equals(parser.getNick())){ //if i'm leaving
                tabbedPane.remove(indexOfChannel);
                return;
            }
            else
            {
                Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                ((ChannelPanel)aComponent).insertString("*** "+parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has left the channel (" + parser.getTrailing()+ ")" , "doc");
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
            //TODO: Private messages from users
            String channelName = parser.getMiddle();
           
            if (channelName.equals(nick))
            {
                //System.out.println(parser.getNick()+ "| "+parser.getParams()+" "+parser.getPrefix()+" "+parser.getServer()+" "+parser.getUser());
                channelName = parser.getNick();
                int indexOfChannel = findTab(tabbedPane, channelName);
                if (indexOfChannel == -1)
                {
                    if (parser.getTrailing().trim().equals("VERSION"))
                    {
                        this.send("NOTICE "+channelName+" "+"\001AlphaClient:v0.1:LM17\001");
                        return;
                        
                    }
                    ChannelPanel channel = new ChannelPanel(channelName, nick, this);
                    channel.insertString("<"+channelName+">: "+parser.getTrailing(), "doc");
                    return;
                }
                Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                ((ChannelPanel)aComponent).insertString((formatNickname("<" + parser.getNick() + ">: ") + parser.getTrailing()).trim(), "doc");
                return;
            }
            
            
            int indexOfChannel = findTab(tabbedPane, channelName);
            if (indexOfChannel == -1)
            {
                ChannelPanel channel = new ChannelPanel(channelName, nick, this);
                channel.insertString("<"+channelName+">: "+parser.getTrailing(), "doc");
                return;
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ((ChannelPanel)aComponent).insertString((formatNickname("<" + parser.getNick() + ">: ") + parser.getTrailing()).trim(), "doc");   
            return;
        }
        if (command.equals("QUIT"))
        {   String quitter = parser.getNick().trim();
            String quitMessage = parser.getParams().substring(2);
            for (int i = 0; i < tabbedPane.getTabCount(); i++){
                Component aComponent = tabbedPane.getComponentAt(i);
                ChannelPanel channel = ((ChannelPanel)aComponent);
                boolean success = channel.removeFromUserList(quitter);
                if (success) channel.insertString("*** "+quitter+" has left the server ("+quitMessage+")", "doc");
            }
            return;            
        }
        if (command.equals("TOPIC"))
        {
            String channelName = parser.getMiddle();
            int indexOfChannel = findTab(tabbedPane, channelName);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.topic = parser.getTrailing();
            
            if (nick.equals(parser.getNick()))
            {
                channel.insertString("*** You set the channel topic to: "+ channel.topic, "doc");
            }
            else channel.insertString(parser.getNick()+" has changed the topic to: "+channel.topic, "doc");
            return;
            
        }
        if (command.equals("WHOIS"))
        {
            
        }
        if (command.equals("001") || command.equals("002") || command.equals("003") || command.equals("004") || command.equals("005"))
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(tabbedPane, host);
            if (indexOfChannel == -1)
            {
                new ChannelPanel(host, nick, this);
                indexOfChannel = findTab(tabbedPane, host);
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Welcome] "+parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("042"))
        {
            //unique id
            return;
        }
	if(command.equals("251") || command.equals("252") || command.equals("253") || command.equals("254") || command.equals("255") || 
                command.equals("256") || command.equals("257") || command.equals("258") || command.equals("259"))
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(tabbedPane, host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Users] "+parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("265") || command.equals("266"))
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(tabbedPane, host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Users] "+parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("301"))
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" "+info, "doc");             
        }
        if (command.equals("307"))
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" "+info,"doc"); 
        }
        if (command.equals("310"))
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String modes = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" "+modes, "doc");
            return;
        }
        if (command.equals("311"))
        {
            //Whois user
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String fulladd = s[2]+"@"+s[3]+" ("+parser.getTrailing().trim()+")";
            
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" is "+fulladd, "doc");
            return;
        
        }
        if (command.equals("312"))
        {
            //whois server
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            String srv = parser.getPrefix();
            
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" is online via "+srv+" ("+info+")", "doc");        
            return;
            
        }
        if (command.equals("313"))
        {
            //whois operator
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" is an IRC Operator", "doc");        
            return;
        }
        if (command.equals("315"))
        {
            //END OF /WHO
        }
        if (command.equals("317"))
        {
            System.out.println(parser.getCommand() +" | "+parser.getHost()+" | "+parser.getMiddle()+" | " + parser.getNick()+" | "+parser.getParams()+" | "+parser.getPrefix()+ " | "+ parser.getServer()+" | "+parser.getTrailing()+" | "+parser.getUser());                       
            //whois idletime
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
            
            channel.insertString("[Whois] "+target+" has been idle for "+idleTime+" seconds", "doc");
            channel.insertString("[Whois] "+target+" has been online since "+channel.signOnTime, "doc");
            
        }
        if (command.equals("318"))
        {
            //End of /WHOIS list.
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("319"))
        {
            //whois channels
            String[] s = parser.getParams().split(" ");  
            String target = s[2];
            String chans = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" is a user on channels: "+chans, "doc"); 
            return;
            
        }
        if (command.equals("331"))
        {
            // 331: No topic
            return;
        }
        if (command.equals("332"))
        {
            //  332: Topic
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);

            int indexOfChannel = findTab(tabbedPane, channelName);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.topic = parser.getTrailing();
            channel.insertString("Current Topic: "+channel.topic, "doc");
            return;
        }
        if (command.equals("333"))
        {
            // 333: Time of topic change and who changed it
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
            int indexOfChannel = findTab(tabbedPane, channelName);
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
        if (command.equals("338"))
        {
            //whois actually
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" "+info, "doc");            
                    
        }
        if (command.equals("352"))
        {
            //WHO command
        }
        if (command.equals("353"))
        {
            //names command
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);
            int indexOfChannel = findTab(tabbedPane, channelName);
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
        if (command.equals("366"))
        {
            //end of names command
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);
            int indexOfChannel = findTab(tabbedPane, channelName);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel); 
            ChannelPanel channel = ((ChannelPanel)aComponent);
            
            channel.removeAllFromuserList();
            channel.userSet.addAll(channel.list);
            channel.list.clear();
            Iterator<String> iterator = channel.userSet.iterator();
            while (iterator.hasNext())
            {
                String nextElement = iterator.next();
                channel.insertString(nextElement, "userList");
            }
            channel.population = channel.userSet.size();
            if (channel.isShowing()) tabInfo.setText(Integer.toString(channel.population)+" nicks     ");

            return;
            }
        if (command.equals("371") || command.equals("372") || command.equals("374") || command.equals("375") || command.equals("376"))
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(tabbedPane, host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[MOTD] "+parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("403"))
        {
            //no such channel
            return;
        }
        if (command.equals("421"))
        {
            //unknown command
            return;
        }
        if (command.equals("437"))
        {
            //:irc.sxci.net 437 rieux #news :Cannot change nickname while banned or moderated on channel
        }
        if (command.equals("439"))
        {
            //target change too fast
            return;            
        }
        if (command.equals("443"))
        {
            //username taken?
        }
        if (command.equals("451"))
        {
            //you have not registered
            return;
        }
        if (command.equals("461"))
        {
            //not enough parameters
            return;
        }
        if (command.equals("671")){
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" "+info,"doc");                   
        }
        else
        {
            Component aComponent = tabbedPane.getComponentAt(0);
            ((ChannelPanel)aComponent).insertString(parser.getCommand(), "doc");
        }        
    }
    
    public void run()
    {
        try {
            socket = new Socket(server, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String line;
            while (socket.isConnected()){
                send("NICK "+nick);
                send("USER "+nick+"123"+" 8 * : some guy");
                send("join #lmitb\r\n");
                while ((line = reader.readLine()) != null){
                    parseFromServer(line);
                }
            }
        
     } catch (IOException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadLocationException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
}

