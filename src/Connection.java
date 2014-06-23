import java.awt.*;
import javax.swing.text.BadLocationException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;


public class Connection implements Runnable{
    Thread thread;
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    String server, host, password;
    boolean autoconnect = true;
    static String[] nicks = {"", "", ""};
    static String currentNick = "", real = "", awayMessage="";
    int port;
    static JTabbedPane tabbedPane;
    static JLabel tabInfo;
    

    public Connection(String server, int port) //need nick and password eventually
    { 
       this.server = server;
       this.port = port;
       
       thread = new Thread(this);
       thread.start();
    }
    public Connection(String server, int port, boolean autoconnect)
    {
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
    public static int findTab(String title)
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
        Parser parser = new Parser(line);
        String command = parser.getCommand();
        //System.out.println(line);
        System.out.println(parser.toString());
        if (command.equals("AWAY"))
        {
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab("#" + channelName);
            if (indexOfChannel == -1)
            {
                System.out.println(parser.toString());
            } 
            ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(indexOfChannel);
            channel.insertString(parser.getTrailing(), ChannelPanel.serverColor);
            return;
        }
        if (command.equals("JOIN"))
        {
           if (currentNick.equals(parser.getNick())) //if joined is me
           {
               String channelName = parser.getTrailing();
               if (channelName.equals("")) channelName = parser.getParams().trim();
               if (channelName.startsWith("#"))
               {
                   int indexOfChannel = findTab(channelName);
                   if (indexOfChannel == -1)
                   {
                       ChannelPanel c = new ChannelPanel(channelName, currentNick, this);
                       int newTabIndex = findTab(c.name);
                       tabbedPane.setSelectedIndex(newTabIndex);                     
                       return;
                   }
                   else{ //joining a room for which you already have a tab (i.e. you were kicked or lost connection or something)
                       
                       Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                       ChannelPanel channel = ((ChannelPanel)aComponent);
                       channel.insertString("Joined "+channelName,ChannelPanel.serverColor);
                   }
               }
           }
           else //if joined isn't me
           {
               String channelName = parser.getTrailing();
               if (channelName.startsWith("#"))
               {
                   int indexOfChannel = findTab(channelName);
                   if (indexOfChannel != -1)
                   {
                       Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                       ChannelPanel channel = ((ChannelPanel)aComponent);
                       channel.addToUserList(parser.getNick());
                       channel.insertString("--> "+parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() +  ") has joined the channel.", ChannelPanel.serverColor);
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
            
            int indexOfChannel = findTab(channelName);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = (ChannelPanel)aComponent;
                        
            if (kicked.equals(currentNick)) //i am kicked
            {
               channel.insertString("*** You have been kicked from the channel by "+kickedBy+ " ("+kickMessage+")", ChannelPanel.serverColor);
               channel.clear();
               return;
            }
            if (kickedBy.equals(currentNick)) //i kick somebody
            {
                channel.insertString("*** You have kicked "+kicked+" from the channel ("+kickMessage+")", ChannelPanel.serverColor);
                channel.removeFromUserList(kicked);
                return;
            }
            else //somebody else kicked
            {
                channel.insertString("*** "+kicked+" was kicked from the channel ("+kickMessage, ChannelPanel.serverColor);
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
                channel.insertString("[Mode] You have set personal modes: "+parser.getTrailing(), ChannelPanel.serverColor);
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

                int indexOfChannel = findTab(chan);
                Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                ChannelPanel channel = ((ChannelPanel)aComponent);

                if (!receiver.equals(""))
                {
                channel.insertString("*** "+giver+" "+power+" "+receiver, ChannelPanel.serverColor);
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
                    channel.insertString("*** "+"Channel mode set to: "+power, ChannelPanel.serverColor); //channel mode set by nobody
                    return;
                }
                else
                {
                    channel.insertString("*** "+giver+" set the channel to: "+power, ChannelPanel.serverColor); //channel mode
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
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", ChannelPanel.serverColor);
                        return;
                    }
                    if (channel.contains("@"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("@"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", ChannelPanel.serverColor);
                        return;
                    }
                    if (channel.contains("~"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("~"+newNick);
                        channel.insertString("*** "+oldNick+" is now known as "+newNick+".", ChannelPanel.serverColor);
                        return;
                    }
                    if (channel.contains("+"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("+"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", ChannelPanel.serverColor);        
                        return;
                    }
                    if (channel.contains("%"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("%"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", ChannelPanel.serverColor);  
                        return;
                    }
                    if (channel.contains("&"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("&"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", ChannelPanel.serverColor);   
                        return;
                    }
                }
                return;
            }
            else //if you change your name
            {
                for (int i = 0; i < tabbedPane.getTabCount(); i++)
                {
                    Component aComponent = tabbedPane.getComponentAt(i);
                    ChannelPanel channel = ((ChannelPanel)aComponent);
                    channel.removeFromUserList(oldNick);
                    this.currentNick = prefix+newNick.substring(1);
                    channel.addToUserList(this.currentNick);
                    channel.insertString("*** You are now known as "+newNick, ChannelPanel.serverColor);
                }
                return;
            }
        }
        if (command.equals("NOTICE")) 
        {
            String h = parser.getPrefix();
            int indexOfChannel = findTab(h);
            ChannelPanel channel;
            
            if (indexOfChannel == -1) channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            else channel = (ChannelPanel)tabbedPane.getComponentAt(indexOfChannel);
            
            if (channel == null){
                return; //this is shit
            }
            
            String nick = parser.getNick();
            
            if (!nick.equals("")){
                channel.insertString("[Notice] -"+nick+"- "+parser.getTrailing(), ChannelPanel.connectColor);
            }
            else{
                channel.insertString("[Notice] "+parser.getTrailing(), ChannelPanel.connectColor);
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
            
            int indexOfChannel = findTab(channelName);
            
            if (currentNick.equals(parser.getNick())){ //if i'm leaving
                tabbedPane.remove(indexOfChannel);
                return;
            }
            else
            {
                Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                ((ChannelPanel)aComponent).insertString("<-- "+parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has left the channel (" + parser.getTrailing()+ ")" , ChannelPanel.serverColor);
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
            String channelName = parser.getMiddle();
           
            if (channelName.equals(currentNick))
            {
                channelName = parser.getNick();
                int indexOfChannel = findTab(channelName);
                if (indexOfChannel == -1)
                {
                    if (parser.getTrailing().trim().equals("VERSION"))
                    {
                        this.send("NOTICE "+channelName+" "+"\001AlphaClient:v0.1:LM17\001");  //placeholder
                        return;
                    }
                    ChannelPanel channel = new ChannelPanel(channelName, currentNick, this);
                    channel.setRightComponent(null);
                    channel.setDividerSize(0);
                    channel.insertString("<"+channelName+">: "+parser.getTrailing(), ChannelPanel.chatColor);
                    return;
                }
                Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                ((ChannelPanel)aComponent).insertString(("<" + parser.getNick() + ">: " + parser.getTrailing()).trim(), ChannelPanel.chatColor);
                return;
            }
            
            
            int indexOfChannel = findTab(channelName);
            if (indexOfChannel == -1)
            {
                ChannelPanel channel = new ChannelPanel(channelName, currentNick, this);
                channel.insertString("<"+channelName+">: "+parser.getTrailing(), ChannelPanel.chatColor);
                return;
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ((ChannelPanel)aComponent).insertString(("<" + parser.getNick() + ">: " + parser.getTrailing()).trim(), ChannelPanel.chatColor);   
            return;
        }
        if (command.equals("QUIT"))
        {   String quitter = parser.getNick().trim();
            String quitMessage = parser.getParams().substring(2);
            for (int i = 0; i < tabbedPane.getTabCount(); i++){
                Component aComponent = tabbedPane.getComponentAt(i);
                ChannelPanel channel = ((ChannelPanel)aComponent);
                boolean success = channel.removeFromUserList(quitter);
                if (success) channel.insertString("<-- "+quitter+" has left the server ("+quitMessage+")", ChannelPanel.serverColor);
            }
            return;            
        }
        if (command.equals("TOPIC"))
        {
            String channelName = parser.getMiddle();
            int indexOfChannel = findTab(channelName);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.topic = parser.getTrailing();
            
            if (currentNick.equals(parser.getNick()))
            {
                channel.insertString("*** You set the channel topic to: "+ channel.topic, ChannelPanel.serverColor);
            }
            else channel.insertString(parser.getNick()+" has changed the topic to: "+channel.topic, ChannelPanel.serverColor);
            return;
            
        }
        if (command.equals("WHOIS"))
        {
         //does this even exist?   
        }
        if (command.equals("001") || command.equals("002") || command.equals("003") || command.equals("004") || command.equals("005"))
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host);
            if (indexOfChannel == -1)
            {
                ChannelPanel c = new ChannelPanel(host, currentNick, this);
                indexOfChannel = findTab(host);
                c.server = parser.getPrefix();
            }
            if (command.equals("001" ) && this.autoconnect )
            {
                for (int i = 0; i < GUI.savedServers.size(); i++)
                {
                    String[] s = GUI.savedServers.get(i).split(","); //this setion auto joins channels saved in savedServers
                    if (s[1].equals(this.server))
                    {
                        String[] c = (s[s.length-3]).trim().split(" ");
                        for (int j = 0; j < c.length; j++)
                        {
                             send("JOIN "+c[j]);
                        }
                    }
                }
                
                
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Welcome] "+parser.getTrailing(), ChannelPanel.connectColor);
            return;
        }
        if (command.equals("042")) //unique id
        {
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
        if (command.equals("251") || command.equals("255"))
        {
            String p = parser.getParams();
            String message = p.substring(p.indexOf(":")+1);
            
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Users] "+message+".", ChannelPanel.connectColor);
            return;
        }
        if (command.equals("252") || command.equals("253") || command.equals("254"))
        {
            String[] s = parser.getParams().trim().split(" ");
            String digit = s[1];
            String msg = parser.getParams().substring(parser.getParams().indexOf(":")+1);
            
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Users] "+digit+" "+msg+".", ChannelPanel.connectColor);
            return;
        }
	if (command.equals("256") || command.equals("257") || command.equals("258") || command.equals("259")) //placeholders
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Users] "+parser.getTrailing(), ChannelPanel.connectColor);
            return;
        }
        if (command.equals("263")) //server load too heavy. please try again (happens with /list)
        {
            
        }
        if (command.equals("265") || command.equals("266"))
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Users] "+parser.getTrailing(), ChannelPanel.connectColor);
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
            channel.insertString("[Whois] "+target+" is away: "+info, ChannelPanel.serverColor);             
        }
        if (command.equals("305")) //no longer away
        {
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            channel.insertString("[Away] You are no longer marked as away.", ChannelPanel.serverColor);
            return;
        }
        if (command.equals("306")) //away
        {
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            channel.insertString("[Away] You have been marked as away (Reason: "+ChannelPanel.awayMessage+")", ChannelPanel.serverColor);
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
            channel.insertString("[Whois] "+target+" "+info, ChannelPanel.serverColor); 
        }
        if (command.equals("310"))
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String modes = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" "+modes, ChannelPanel.serverColor);
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
            channel.insertString("[Whois] "+target+" is "+fulladd, ChannelPanel.serverColor);
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
            channel.insertString("[Whois] "+target+" is online via "+srv+" ("+info+")", ChannelPanel.serverColor);        
            return;
            
        }
        if (command.equals("313")) //whois operator
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" is an IRC Operator", ChannelPanel.serverColor);        
            return;
        }
        if (command.equals("315")) //end of /who
        {
            String s = parser.getMiddle().split(" ")[1];
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            channel.insertString("[Who] End of /WHO list for "+s, ChannelPanel.serverColor);
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
            
            channel.insertString("[Whois] "+target+" has been idle for "+idleTime+" seconds", ChannelPanel.serverColor);
            channel.insertString("[Whois] "+target+" has been online since "+channel.signOnTime, ChannelPanel.serverColor);
            
        }
        if (command.equals("318")) //end of whois
        {
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+parser.getTrailing(), ChannelPanel.serverColor);
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
            channel.insertString("[Whois] "+target+" is a user on channels: "+chans, ChannelPanel.serverColor); 
            return;
            
        }
        if (command.equals("331")) //no topic
        {
            //channel.server = parser.getPrefix();
            return;
        }
        if (command.equals("332")) //topic
        {
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);

            int indexOfChannel = findTab(channelName);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.topic = parser.getTrailing();
            channel.server = parser.getPrefix();
            channel.insertString("Current Topic: "+channel.topic, ChannelPanel.serverColor);
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
            int indexOfChannel = findTab(channelName);
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
            channel.insertString("[Whois] "+target+" "+info, ChannelPanel.serverColor);            
                    
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
            String msg = "[Who] "+person+" is "+s[2]+"@"+s[3]+" ("+parser.getTrailing().substring(2)+")";
            ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
            channel.insertString(msg, ChannelPanel.serverColor);
            
        }
        if (command.equals("353")) //names command
        {
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);
            int indexOfChannel = findTab(channelName);
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
            int indexOfChannel = findTab(channelName);
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
        if (command.equals("371") || command.equals("372") || command.equals("374") || command.equals("375") || command.equals("376"))
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            if (command.equals("372")) channel.server = host;
            channel.insertString("[MOTD] "+parser.getTrailing(), ChannelPanel.connectColor);
            return;
        }
        if (command.equals("401")) //no such nick/channel
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            channel.insertString("[Error] "+s[1]+": No such nick/channel.", ChannelPanel.errorColor);
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
            channel.insertString("[Error] "+chan+": No such channel. ", ChannelPanel.errorColor);
        }
        if (command.equals("404"))
        {
            String[] s = parser.getParams().split(" ");
            String chan = s[1].trim();
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            channel.insertString("[Error] "+chan+": Cannot send to channel.", ChannelPanel.errorColor);
            return;          
        }
        if (command.equals("405")) //joined too many channels
        {
            String[] s = parser.getParams().trim().split(" ");
            String chan = s[1].trim();
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            channel.insertString("[Error] "+chan+": You have joined too many channels.", ChannelPanel.errorColor);
            return;
        }
        if (command.equals("412")) //no text to send
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            channel.insertString("[Error] "+s[0]+" no text to send.", ChannelPanel.errorColor);
            return;
        }
        if (command.equals("421")) //unknown command
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            channel.insertString("[Error] "+s[1]+": Unknown command.", ChannelPanel.errorColor);
            return;
        }
        if (command.equals("432")) //eroneous nickname
        {
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            channel.insertString("[Nick] Erroneous Nickname", ChannelPanel.errorColor);
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
                channel.insertString("[Error] All your nicks are taken", ChannelPanel.errorColor);
                channel.connection.send("quit");
                return;
            }
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            channel.insertString("[Error] **"+parser.getParams().trim(), ChannelPanel.errorColor);
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
                new ChannelPanel(parser.getPrefix(), this.currentNick, this);
                indexOfChannel = findTab(parser.getPrefix());
                
            }
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getComponentAt(indexOfChannel));
            channel.server = parser.getPrefix();
            channel.insertString("Please wait while we process your connection...", ChannelPanel.connectColor);
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
            channel.insertString("[Whois] "+target+" "+info, ChannelPanel.serverColor);                   
        }
        else
        {
            System.out.println(line);
            //Component aComponent = tabbedPane.getComponentAt(0);
            //((ChannelPanel)aComponent).insertString(parser.getCommand(), ChannelPanel.serverColor);
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