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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
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
    String server, host, nick = "rieux";
    int port;
    DefaultStyledDocument doc, userList;
    JTabbedPane tabbedPane;
    JLabel tabInfo;
    

    public Connection(String server, int port, DefaultStyledDocument doc, DefaultStyledDocument userList, JTabbedPane tabbedPane, JLabel tabInfo)
    {
       this.server = server;
       this.port = port;
       this.doc = doc;
       this.userList = userList;
       
       
       this.tabbedPane = tabbedPane;
       this.tabInfo = tabInfo;
       
       ChannelPanel first = new ChannelPanel("Rizon");
       tabbedPane.add(first.name, first);
       
       thread = new Thread(this);
       thread.start();
    }     
    public void send(String line) throws IOException
    {
        try{ 
            writer.write(line+"\r\n");
            writer.flush();
        } catch (IOException e){
            //do nothing
        }
    }
    public String formatNickname(String nickname)
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
    private int findTab(JTabbedPane tabbedPane, String title)
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
                       ChannelPanel c = new ChannelPanel(channelName);
                       tabbedPane.add(c.name, c);
                       int newTabIndex = findTab(tabbedPane, c.name);
                       tabbedPane.setSelectedIndex(newTabIndex);                     
                       return;
                   }
                   else
                   {
                        System.out.println("do we ever even get here tho?");
                        Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                        ((ChannelPanel)aComponent).insertString(parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() + "has joined the channel", "doc");   
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
                       if (aComponent instanceof JSplitPane)
                       {
                           ((ChannelPanel)aComponent).addToUserList(parser.getNick());
                           ((ChannelPanel)aComponent).insertString(parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() +  ") has joined the channel.", "doc");
                           return;
                       }
                   }
                   else
                   {
                       System.out.println("___error in JOIN___");
                       return;
                   }
               }
           }
           ChannelPanel channel;
           channel = new ChannelPanel(parser.getTrailing());
           tabbedPane.add(channel, parser.getTrailing());
           channel.insertString(parser.getTrailing(), parser.getNick() + " joined in " + parser.getTrailing());
        }
        if (command.equals("NICK"))
        {
            String oldNick = parser.getNick();
            String newNick = parser.getTrailing();
            if (!nick.equals(oldNick)) //if someone else changes name
            {
                for (int i = 0; i < tabbedPane.getTabCount(); i++)
                {
                    Component aComponent = tabbedPane.getComponentAt(i);
                    ChannelPanel channel = ((ChannelPanel)aComponent);
                    
                    if (channel.userSet.contains(oldNick))
                    {
                        System.out.println("attempting to remove "+oldNick);
                        channel.removeFromUserList(oldNick);
                        channel.addToUserList(newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc");
                        return;
                    }
                    if (channel.userSet.contains("@"+oldNick))
                    {
                        System.out.println("attempting to remove "+"@"+oldNick);
                        channel.removeFromUserList("@"+oldNick);
                        channel.addToUserList("@"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc");
                        return;
                    }
                    if (channel.userSet.contains("+"+oldNick))
                    {
                        channel.removeFromUserList("+"+oldNick);
                        channel.addToUserList("+"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc");        
                        return;
                    }
                    if (channel.userSet.contains("%"+oldNick))
                    {
                        channel.removeFromUserList("%"+oldNick);
                        channel.addToUserList("%"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc");  
                        return;
                    }
                    if (channel.userSet.contains("&"+oldNick))
                    {
                        channel.removeFromUserList("&"+oldNick);
                        channel.addToUserList("&"+newNick);
                        channel.insertString("*** "+ oldNick+" is now known as "+newNick+".", "doc");   
                        return;
                    }
                }
                return;
            }
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
                ((ChannelPanel)aComponent).insertString(parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has left the server (" + parser.getTrailing()+ ")" , "doc");
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
            int indexOfChannel = findTab(tabbedPane, channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0); 
                ((ChannelPanel)aComponent).insertString("___PRIVMSG BROKEN___"+line, "doc");
                return;
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ((ChannelPanel)aComponent).insertString((formatNickname("<" + parser.getNick() + ">: ") + parser.getTrailing()).trim(), "doc");   
            return;
        }
        if (command.equals("QUIT"))
        {
            if (!nick.equals(parser.getNick())){ //if i'm leaving
                for (int i = 0; i < tabbedPane.getTabCount(); i++){
                    Component aComponent = tabbedPane.getComponentAt(i);
                    ChannelPanel channel = ((ChannelPanel)aComponent);
                    channel.removeFromUserList(parser.getNick());
                }
                return;
            }
            //else close all connections 
            
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
        if (command.equals("001") || command.equals("002") || command.equals("003") || command.equals("004") || command.equals("005"))
        {
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                ((ChannelPanel)aComponent).insertString("___"+line, "doc");
            }
            Component aComponent = tabbedPane.getComponentAt(0);
            ((ChannelPanel)aComponent).insertString(parser.getTrailing(), "doc");
            return;
        }
	if(command.equals("251") || command.equals("252") || command.equals("253") || command.equals("254") || command.equals("255") || 
                command.equals("256") || command.equals("257") || command.equals("258") || command.equals("259"))
        {
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                ((ChannelPanel)aComponent).insertString("___"+line, "doc");
            }
            Component aComponent = tabbedPane.getComponentAt(0);
            ((ChannelPanel)aComponent).insertString(parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("265") || command.equals("266"))
        {
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                ((ChannelPanel)aComponent).insertString("___"+line, "doc");
            }
            Component aComponent = tabbedPane.getComponentAt(0);
            ((ChannelPanel)aComponent).insertString(parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("331"))
        {
            // 331: No topic
            return;
        }
        if (command.equals("332"))
        {/*
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
            return;*/
        }
        if (command.equals("333"))
        {
            /*
            // 333: Time of topic change and who changed it
            String channelName = parser.getParams();
            int index1 = channelName.indexOf("#");
            String chan = channelName.substring(index1);
            int index2 = chan.indexOf(" ");
            channelName = chan.substring(0,index2);
            
            int indexOfChannel = findTab(tabbedPane, channelName);
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ChannelPanel channel = ((ChannelPanel)aComponent);
     
            long unixTime = System.currentTimeMillis() / 1000L;   */      
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
            
            channel.userList.remove(0, userList.getLength());
            channel.userSet.addAll(channel.list);
            channel.list = new ArrayList<String>();
            Iterator<String> iterator = channel.userSet.iterator();
            while (iterator.hasNext()){
                String nextElement = iterator.next();
                channel.insertString(nextElement, "userList");
            }
            channel.population = channel.userSet.size();
            return;
            }
        if (command.equals("371") || command.equals("372") || command.equals("374") || command.equals("375") || command.equals("376"))
        {
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                ((ChannelPanel)aComponent).insertString(line, "doc");
            }
            Component aComponent = tabbedPane.getComponentAt(0);
            ((ChannelPanel)aComponent).insertString(parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("451")){
            //you have not registered
            
        }
        if (command.equals("471"))
        {
            //unknown command
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                ((ChannelPanel)aComponent).insertString("___"+line, "doc");
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            ((ChannelPanel)aComponent).insertString(parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("439"))
        {
            //Message spamming
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                //System.out.println(aComponent.toString());
                ((ChannelPanel)aComponent).insertString(line, "doc");
                return;
            }
            Component aComponent = tabbedPane.getComponentAt(0);
            ((ChannelPanel)aComponent).insertString(parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("451"))
            //you have not registered
        {
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                ((ChannelPanel)aComponent).insertString("___"+line, "doc");
            }
            Component aComponent = tabbedPane.getComponentAt(0);
            ((ChannelPanel)aComponent).insertString(parser.getTrailing(), "doc");
            return;
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
                writer.write("NICK "+ nick+"\r\n");
                writer.write("USER "+nick+" 8 * : a bot\r\n");
                writer.write("join #lmitb\r\n");
                writer.flush();
                while ((line = reader.readLine()) != null){
                    //System.out.println(line);
                    parseFromServer(line);
                }
            }
        
     } catch (IOException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadLocationException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
       public class ChannelPanel extends JSplitPane implements ActionListener{
           
        final String name; 
        String topic;
        int population, ops;
        DefaultStyledDocument userList = new DefaultStyledDocument(), doc = new DefaultStyledDocument();
        final JTextField chatInputPane = new JTextField();
        final JTextPane chatPane = new JTextPane(doc), userListPane = new JTextPane(userList);
        final JScrollPane jScrollPane1 = new JScrollPane(), jScrollPane2 = new JScrollPane();
        
        SortedSet<String> userSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        ArrayList<String> list = new ArrayList<String>();
    
        public ChannelPanel(String name)
        {
            this.name = name;
            makePanel();
        }
        private void makePanel()
        {
        tabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        tabbedPane.setToolTipText("");
        tabbedPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tabbedPane.setFocusable(false);
        tabbedPane.setPreferredSize(new java.awt.Dimension(600, 450));

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setDividerLocation(480);
        setResizeWeight(1.0);
        setVerifyInputWhenFocusTarget(false);

        
        chatPane.setEditable(false);
        jScrollPane1.setViewportView(userListPane);
        jScrollPane2.setViewportView(chatPane);
        DefaultCaret caret = (DefaultCaret)chatPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        setLeftComponent(jScrollPane2);
        setRightComponent(jScrollPane1);
        
        userListPane.setEditable(false);
        userListPane.setAutoscrolls(false);
        userListPane.setFocusable(false);
        userListPane.setMaximumSize(new java.awt.Dimension(25, 25));
        
        ChangeListener changeListener = new ChangeListener(){
            public void stateChanged(ChangeEvent changeEvent){
                javax.swing.JTabbedPane tabbedPane = (javax.swing.JTabbedPane)changeEvent.getSource();
                int index = tabbedPane.getSelectedIndex();
                ChannelPanel c = (ChannelPanel)tabbedPane.getComponentAt(index);
                tabInfo.setText(c.topic+"  |  "+Integer.toString(c.population)+" nicks     ");
            }          
        };
        tabbedPane.addChangeListener(changeListener);   
        
        }
        private void insertString(String line, String target) throws BadLocationException
        { 
            if (target.equals("doc")){
                doc.insertString(doc.getLength(), line+"\n", null);
                return;
            }
            if (target.equals("userList")){
                userList.insertString(userList.getLength(), line+"\n", null);
                return;
            }
            else System.out.println("_____________________insertString broken_____________________");
        }
        private void addToUserList(String nick) throws BadLocationException
        {
            userList.remove(0, userList.getLength());
            userSet.add(nick);
            population = userSet.size();
            Iterator<String> iterator = userSet.iterator();
            while (iterator.hasNext()){
                String nextElement = iterator.next();
                insertString(nextElement, "userList");
            }
            return;
        }
        private void removeFromUserList(String nick) throws BadLocationException
        {
            userList.remove(0, userList.getLength());
            userSet.remove(nick);
            population = userSet.size();
            Iterator<String> iterator = userSet.iterator();
            while (iterator.hasNext()){
                String nextElement = iterator.next();
                insertString(nextElement, "userList");
            }
            return;
         }       
        public void actionPerformed(ActionEvent e)
        {
            //unimplemented
            
        }
        
    }
            
}

