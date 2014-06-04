import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.text.DefaultCaret;

public class Connection implements Runnable{
    Thread thread;
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    FileWriter chatLog;
    String server, host, nick = "rieux", channel = "";
    int port;
    DefaultStyledDocument doc, userList;
    public ArrayList<String> list;
    SortedSet<String> set;
    JTabbedPane tabbedPane;

    public Connection(String server, int port, DefaultStyledDocument doc, DefaultStyledDocument userList, JTabbedPane tabbedPane)
    {
       this.server = server;
       this.port = port;
       this.doc = doc;
       this.userList = userList;
       
       this.tabbedPane = tabbedPane;
       
       ChannelPanel first = new ChannelPanel("main");
       tabbedPane.add("main", first);
       
       list = new ArrayList<String>();
       set = new TreeSet(String.CASE_INSENSITIVE_ORDER);
       
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
        if (command.equals("AWAY")){
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                ChannelPanel c = (ChannelPanel)tabbedPane.getComponentAt(0);
                c.insertString("___"+line, "doc");
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
                   //channelName = channelName.substring(1);
                   int indexOfChannel = findTab(tabbedPane, channelName);
                   
                   if (indexOfChannel == -1)
                   {
                       System.out.println("adding channel: "+channelName);
                       ChannelPanel c = new ChannelPanel(channelName);
                       tabbedPane.add(c.name, c);
                       return;
                   }
                   else
                   {
                        System.out.println("do we ever even get here tho?");
                        Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
                        ((ChannelPanel)aComponent).insertString(parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() + "has joined the channel", "doc");   
                        return;
                       //ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(indexOfChannel);
                       //channel.insertString(parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() + "has joined the channel", "doc");

                   }
               }
           }
           else //if joined isn't me
           {
               String channelName = parser.getTrailing();
               if (channelName.startsWith("#"))
               {
                   //channelName = channelName.substring(1);
                   int indexOfChannel = findTab(tabbedPane, channelName);
                   if (indexOfChannel != -1)
                   {
                       System.out.println("promiseland");
                       Component aComponent = tabbedPane.getComponent(indexOfChannel);
                       if (aComponent instanceof JSplitPane)
                       {
                            System.out.println("debug: "+aComponent.toString());
                            ChannelPanel cc = (ChannelPanel)aComponent;
                            cc.insertString("testing", "doc");
                            ((ChannelPanel)aComponent).insertString(parser.getNick() + " (" + parser.getUser() + "@" + parser.getHost() +  ") has joined the channel " + parser.getTrailing(), "doc");
                            return;
                       }
                      // else System.out.println("missed a join");
                       //return;
                   }
                   else
                   {
                       System.out.println("wtf");
                       return;
                   }
               }
           }
           ChannelPanel channel;
           channel = new ChannelPanel(parser.getTrailing());
           tabbedPane.add(channel, parser.getTrailing());
           channel.insertString(parser.getTrailing(), parser.getNick() + " joined in " + parser.getTrailing());
        }
        if (command.equals("PART"))
        {
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                tabbedPane.remove(aComponent);
                ((ChannelPanel)aComponent).insertString("_____PART is broken___"+line, "doc");
                return;
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            //((ChannelPanel)aComponent).removeFromUserList(parser.getNick());
            ((ChannelPanel)aComponent).insertString(parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has left the server " + parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("PING"))
        {
            send("PONG :"+ parser.getTrailing());
            return;
        }
        if (command.equals("PRIVMSG") || command.equals("MSG"))
        {
            String channelName = parser.getMiddle();
            int indexOfChannel = findTab(tabbedPane, channelName);
            //System.out.println("Tab #"+indexOfChannel + "___"+tabbedPane.getTitleAt(2)); 
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
            //user quits
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                ((ChannelPanel)aComponent).insertString("___"+line, "doc");
                return;
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            //((ChannelPanel)aComponent).removeFromUserList(parser.getNick());
            ((ChannelPanel)aComponent).insertString(parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has left the server " + parser.getTrailing(), "doc");
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
        if (command.equals("331") || command.equals("332"))
        {
            //  332: Topic, 331: No topic
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                ((ChannelPanel)aComponent).insertString("___"+line, "doc");
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            if (command.equals("332")) ((ChannelPanel)aComponent).insertString(parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("333"))
        {
            //who set topic and the time
            String channelName = parser.getTrailing();
            int indexOfChannel = findTab(tabbedPane, "#" + channelName);
            if (indexOfChannel == -1)
            {
                Component aComponent = tabbedPane.getComponentAt(0);
                ((ChannelPanel)aComponent).insertString("___"+line, "doc");
            }
            Component aComponent = tabbedPane.getComponentAt(indexOfChannel);
            if (command.equals("333")) ((ChannelPanel)aComponent).insertString(parser.getTrailing(), "doc");
            return;            
        }
       /* if (command.equals("353")){
            //names command
            System.out.println("names list: "+parser.getTrailing());
            String[] nn = parser.getTrailing().split(" ");
            list.addAll(Arrays.asList(nn));
            return;
        }
        if (command.equals("366")){
            //end of names command
            userList.remove(0, userList.getLength());
            set.addAll(list);
            list = new ArrayList<String>();
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()){
                String nextElement = iterator.next();
                insertString(nextElement, userList);
            }
            return;
            }*/
        if (command.equals("371") || command.equals("372") || command.equals("374") || command.equals("375") || command.equals("376"))
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
                ((ChannelPanel)aComponent).insertString("___"+line, "doc");
                return;
            }
            Component aComponent = tabbedPane.getComponentAt(0);
            ((ChannelPanel)aComponent).insertString(parser.getTrailing(), "doc");
            return;
        }
        if (command.equals("451"))
        {
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
        else
        {
            Component aComponent = tabbedPane.getComponentAt(0);
            ((ChannelPanel)aComponent).insertString("else case___"+parser.getCommand(), "doc");
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
        DefaultStyledDocument userList = new DefaultStyledDocument(), doc = new DefaultStyledDocument();
        //final JTextArea chatText = new JTextArea(), usersText = new JTextArea();
        final JTextField chatInputPane = new JTextField();
        final JTextPane chatPane = new JTextPane(doc), userListPane = new JTextPane(userList);
        final JScrollPane jScrollPane1 = new JScrollPane(), jScrollPane2 = new JScrollPane();
        SortedSet<String> userSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

        
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

            
        //tabbedPane.addTab("Main", );
        
        
        }
        private void insertString(String line, String target) throws BadLocationException
        { 
            if (target.equals("doc")) doc.insertString(doc.getLength(), line+"\n", null);
            else userList.insertString(userList.getLength(), line+"\n", null);
        }
        private void addToUserList(String nick) throws BadLocationException
        {
            System.out.println("adding nick "+nick+" to "+name);
            userList.remove(0, userList.getLength());
            userSet.add(nick);
            Iterator<String> iterator = userSet.iterator();
            while (iterator.hasNext()){
                String nextElement = iterator.next();
                insertString(nextElement, "userList");
            }
            return;
        }
        private void removeFromUserList(String nick) throws BadLocationException
        {
            System.out.println("removing "+nick);
            userList.remove(0, userList.getLength());
            boolean exists = set.remove(nick);
            if (exists == false){
                set.remove("+"+nick);
            }
            Iterator<String> iterator = set.iterator();
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

