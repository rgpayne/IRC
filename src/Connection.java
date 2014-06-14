import java.awt.Component;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.swing.text.BadLocationException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class Connection implements Runnable{
    Thread thread;
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    String server, host, password;
    static String nick = "rieux";
    int port;
    static JTabbedPane tabbedPane;
    JLabel tabInfo;
    ChannelPanel first;
    

    public Connection(String server, int port, JTabbedPane tabbedPane, JLabel tabInfo)
    {
       this.server = server;
       this.port = port;
       this.password = password;
       
       this.tabbedPane = tabbedPane;
       this.tabInfo = tabInfo;
       
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
                channel.insertString("___"+line, "doc", ChannelPanel.serverColor);
            } 
            ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(indexOfChannel);
            channel.insertString(parser.getTrailing(), "doc", ChannelPanel.serverColor);
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
                   else System.out.println("_____JOIN BROKEN_____");
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
                       channel.insertString("*** "+parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() +  ") has joined the channel.", "doc", ChannelPanel.serverColor);
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
            
            int indexOfChannel = findTab(tabbedPane, channelName);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = (ChannelPanel)aComponent;
                        
            if (kicked.equals(nick)) //i am kicked
            {
               channel.insertString("*** You have been kicked from the channel by "+kickedBy+ " ("+kickMessage+")", "doc", ChannelPanel.serverColor);
               channel.removeFromUserList(nick);
               channel.removeAllFromuserList();
               return;
            }
            if (kickedBy.equals(nick)) //i kick somebody
            {
                channel.insertString("*** You have kicked "+kicked+" from the channel ("+kickMessage+")", "doc", ChannelPanel.serverColor);
                channel.removeFromUserList(kicked);
                return;
            }
            else //somebody else kicked
            {
                channel.insertString("*** "+kicked+" was kicked from the channel ("+kickMessage, server, ChannelPanel.serverColor);
                channel.removeFromUserList(kicked);
                return;
            }
        }
        if (command.equals("MODE"))
        {           
            System.out.println(parser.toString());
            if (parser.getServer().equals(nick)) //setting personal mode
            {
                Component aComponent = tabbedPane.getSelectedComponent();
                ChannelPanel channel = ((ChannelPanel)aComponent);
                channel.insertString("[Mode] You have set personal modes: "+parser.getTrailing(), "doc", ChannelPanel.serverColor);
            }
            
            //need to account for channel modes  
            
            
            else //setting a user's mode
            { 
                String[] s = parser.getParams().trim().split(" ");
                String receiver = "";
                String giver = parser.getNick();
                String chan = s[0];
                String power = s[1];
                if (s.length >= 3) receiver = s[2];

                int indexOfChannel = findTab(tabbedPane, chan);
                Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                ChannelPanel channel = ((ChannelPanel)aComponent);

                if (!receiver.equals(""))
                {
                channel.insertString("*** "+giver+" "+power+" "+receiver, "doc", ChannelPanel.serverColor);
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
                else
                {
                    channel.insertString("*** "+giver+" set the channel to: "+power, "doc", ChannelPanel.serverColor);
                }
            }
            return;
        }
        if (command.equals("NICK"))
        {
            String prefix = parser.getNick().substring(0,1);
            String oldNick = parser.getNick();
            String newNick = parser.getTrailing();
            
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
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc", ChannelPanel.serverColor);
                        return;
                    }
                    if (channel.userSet.contains("@"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("@"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc", ChannelPanel.serverColor);
                        return;
                    }
                    if (channel.userSet.contains("~"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("~"+newNick);
                        channel.insertString("*** "+oldNick+" is now known as "+newNick+".", "doc", ChannelPanel.serverColor);
                        return;
                    }
                    if (channel.userSet.contains("+"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("+"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc", ChannelPanel.serverColor);        
                        return;
                    }
                    if (channel.userSet.contains("%"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("%"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc", ChannelPanel.serverColor);  
                        return;
                    }
                    if (channel.userSet.contains("&"+oldNick))
                    {
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList("&"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc", ChannelPanel.serverColor);   
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
                    this.nick = prefix+newNick.substring(1);
                    channel.addToUserList(this.nick);
                    channel.insertString("*** You are now known as "+newNick, "doc", ChannelPanel.serverColor);
                    //this.nick = newNick;
                }
                return;
            }
        }
        if (command.equals("NOTICE"))
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(tabbedPane, host);
            if (indexOfChannel == -1) return;
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString(parser.getTrailing(), "doc", ChannelPanel.serverColor);
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
                ((ChannelPanel)aComponent).insertString("*** "+parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has left the channel (" + parser.getTrailing()+ ")" , "doc", ChannelPanel.serverColor);
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
            System.out.println(parser.toString());
            String channelName = parser.getMiddle();
           
            if (channelName.equals(nick))
            {
                channelName = parser.getNick();
                int indexOfChannel = findTab(tabbedPane, channelName);
                if (indexOfChannel == -1)
                {
                    if (parser.getTrailing().trim().equals("VERSION"))
                    {
                        this.send("NOTICE "+channelName+" "+"\001AlphaClient:v0.1:LM17\001");  //placeholder
                        return;
                        
                    }
                    ChannelPanel channel = new ChannelPanel(channelName, nick, this);
                    channel.insertString("("+channelName+"): "+parser.getTrailing(), "doc", ChannelPanel.chatColor);
                    return;
                }
                Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                ((ChannelPanel)aComponent).insertString(("(" + parser.getNick() + "): " + parser.getTrailing()).trim(), "doc", ChannelPanel.chatColor);
                return;
            }
            
            
            int indexOfChannel = findTab(tabbedPane, channelName);
            if (indexOfChannel == -1)
            {
                ChannelPanel channel = new ChannelPanel(channelName, nick, this);
                channel.insertString("("+channelName+"): "+parser.getTrailing(), "doc", ChannelPanel.chatColor);
                return;
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ((ChannelPanel)aComponent).insertString((formatNickname("(" + parser.getNick() + "): ") + parser.getTrailing()).trim(), "doc", ChannelPanel.chatColor);   
            return;
        }
        if (command.equals("QUIT"))
        {   String quitter = parser.getNick().trim();
            String quitMessage = parser.getParams().substring(2);
            for (int i = 0; i < tabbedPane.getTabCount(); i++){
                Component aComponent = tabbedPane.getComponentAt(i);
                ChannelPanel channel = ((ChannelPanel)aComponent);
                boolean success = channel.removeFromUserList(quitter);
                if (success) channel.insertString("*** "+quitter+" has left the server ("+quitMessage+")", "doc", ChannelPanel.serverColor);
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
                channel.insertString("*** You set the channel topic to: "+ channel.topic, "doc", ChannelPanel.serverColor);
            }
            else channel.insertString(parser.getNick()+" has changed the topic to: "+channel.topic, "doc", ChannelPanel.serverColor);
            return;
            
        }
        if (command.equals("WHOIS"))
        {
         //does this even exist?   
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
            channel.insertString("[Welcome] "+parser.getTrailing(), "doc", ChannelPanel.serverColor);
            return;
        }
        if (command.equals("042")) //unique id
        {
            return;
        }
        if (command.equals("251") || command.equals("255"))
        {
            String p = parser.getParams();
            String message = p.substring(p.indexOf(":")+1);
            
            String host = parser.getPrefix();
            int indexOfChannel = findTab(tabbedPane, host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Users] "+message+".", "doc", ChannelPanel.serverColor);
            return;
        }
        if (command.equals("252") || command.equals("253") || command.equals("254"))
        {
            String[] s = parser.getParams().trim().split(" ");
            String digit = s[1];
            String msg = parser.getParams().substring(parser.getParams().indexOf(":")+1);
            
            String host = parser.getPrefix();
            int indexOfChannel = findTab(tabbedPane, host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Users] "+digit+" "+msg+".", "doc", ChannelPanel.serverColor);
            return;
        }
	if (command.equals("256") || command.equals("257") || command.equals("258") || command.equals("259")) //placeholders
        {
            System.out.println(parser.toString());
            String host = parser.getPrefix();
            int indexOfChannel = findTab(tabbedPane, host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Users] "+parser.getTrailing(), "doc", ChannelPanel.serverColor);
            return;
        }
        if (command.equals("265") || command.equals("266"))
        {
            String host = parser.getPrefix();
            int indexOfChannel = findTab(tabbedPane, host);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Users] "+parser.getTrailing(), "doc", ChannelPanel.serverColor);
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
            channel.insertString("[Whois] "+target+" "+info, "doc", ChannelPanel.serverColor);             
        }
        if (command.equals("307"))
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" "+info,"doc", ChannelPanel.serverColor); 
        }
        if (command.equals("310"))
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String modes = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" "+modes, "doc", ChannelPanel.serverColor);
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
            channel.insertString("[Whois] "+target+" is "+fulladd, "doc", ChannelPanel.serverColor);
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
            channel.insertString("[Whois] "+target+" is online via "+srv+" ("+info+")", "doc", ChannelPanel.serverColor);        
            return;
            
        }
        if (command.equals("313")) //whois operator
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" is an IRC Operator", "doc", ChannelPanel.serverColor);        
            return;
        }
        if (command.equals("315")) //end of /who
        {
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
            
            channel.insertString("[Whois] "+target+" has been idle for "+idleTime+" seconds", "doc", ChannelPanel.serverColor);
            channel.insertString("[Whois] "+target+" has been online since "+channel.signOnTime, "doc", ChannelPanel.serverColor);
            
        }
        if (command.equals("318")) //end of whois
        {
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+parser.getTrailing(), "doc", ChannelPanel.serverColor);
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
            channel.insertString("[Whois] "+target+" is a user on channels: "+chans, "doc", ChannelPanel.serverColor); 
            return;
            
        }
        if (command.equals("331")) //no topic
        {
            return;
        }
        if (command.equals("332")) //topic
        {
            String channelName = parser.getParams();
            int index = channelName.indexOf("#");
            int index2 = channelName.indexOf(":");
            if (index2 != -1) channelName = channelName.substring(index, index2 - 1);
            else channelName = channelName.substring(index);

            int indexOfChannel = findTab(tabbedPane, channelName);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.topic = parser.getTrailing();
            channel.insertString("Current Topic: "+channel.topic, "doc", ChannelPanel.serverColor);
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
        if (command.equals("338")) //whois actually
        {
            String[] s = parser.getMiddle().split(" ");
            String target = s[1];
            String info = parser.getTrailing();
            int indexOfChannel = tabbedPane.getSelectedIndex();
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
            channel.insertString("[Whois] "+target+" "+info, "doc", ChannelPanel.serverColor);            
                    
        }
        if (command.equals("352")) //who reply
        {
            
        }
        if (command.equals("353")) //names cmomand
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
                channel.insertString(nextElement, "userList", ChannelPanel.serverColor);
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
            channel.insertString("[MOTD] "+parser.getTrailing(), "doc", ChannelPanel.serverColor);
            return;
        }
        if (command.equals("401")) //no such nick/channel
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            channel.insertString("[Error] "+s[1]+": No such nick/channel.", "doc", ChannelPanel.errorColor);
            return;            
        }
        if (command.equals("402")) //no such server
        {            
        }
        if (command.equals("403")) //no such channel 
        {
            //does this exist?
            return;
        }
        if (command.equals("412")) //no text to send
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            channel.insertString("[Error] "+s[0]+" no text to send", "doc", ChannelPanel.errorColor);
            return;
        }
        if (command.equals("421")) //unknown command
        {
            String[] s = parser.getParams().trim().split(" ");
            ChannelPanel channel = ((ChannelPanel)tabbedPane.getSelectedComponent());
            channel.insertString("[Error] "+s[1]+": Unknown command.", "doc", ChannelPanel.errorColor);
            return;
        }
        if (command.equals("437")) //cannot change nickname while banned o moderated on channel
        {
        }
        if (command.equals("439")) //target change too fast
        {
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
        if (command.equals("461")) //not enough parameters
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
            channel.insertString("[Whois] "+target+" "+info,"doc", ChannelPanel.serverColor);                   
        }
        else
        {
            Component aComponent = tabbedPane.getComponentAt(0);
            ((ChannelPanel)aComponent).insertString(parser.getCommand(), "doc", ChannelPanel.serverColor);
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
                send("NICK "+nick);
                send("USER "+nick+"123"+" 8 * : some guy");
                send("join #lmitb\r\n");
                while ((line = reader.readLine()) != null)
                {
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

