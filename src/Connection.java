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
    
    public Connection(String server, int port, DefaultStyledDocument doc, DefaultStyledDocument userList){
       this.server = server;
       this.port = port;
       this.doc = doc;
       this.userList = userList;
       
       list = new ArrayList<String>();
       set = new TreeSet(String.CASE_INSENSITIVE_ORDER);
       
       thread = new Thread(this);
       thread.start();
    }     
    public void insertString(String line, DefaultStyledDocument target) throws BadLocationException{
        target.insertString(target.getLength(), line+"\n", null);
    }
    public void send(String line) throws IOException{
        try{ 
            writer.write(line+"\r\n");
            writer.flush();
        } catch (IOException e){
            //do nothing
        }
    }
    public String formatNickname(String nickname){
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
    public void addToUserList(String nick) throws BadLocationException{
        System.out.println("adding nick "+nick);
            userList.remove(0, userList.getLength());
            set.add(nick);
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()){
                String nextElement = iterator.next();
                insertString(nextElement, userList);
            }
            return;
    }
    public void removeFromUserList(String nick) throws BadLocationException{
            System.out.println("removing "+nick);
            userList.remove(0, userList.getLength());
            boolean exists = set.remove(nick);
            if (exists == false){
                set.remove("+"+nick);
            }
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()){
                String nextElement = iterator.next();
                insertString(nextElement, userList);
            }
            return;
        
    }
    public void parseFromServer(String line) throws IOException, BadLocationException{
        //TODO : rieux shows up as rieux and @rieux because doesnt distinguish between channels
        Parser parser = new Parser(line);
        String command = parser.getCommand();
        if (command.equals("AWAY")){
            insertString(parser.getTrailing(), doc);
            return;
        }
        if (command.equals("JOIN")){
            //user joins
            if (!parser.getNick().equals(nick)){
                String channelName = parser.getTrailing();
                if (channelName.startsWith("#")){
                    channelName = channelName.substring(1);
                    if (!parser.getUser().equals(nick)){
                        addToUserList(parser.getNick());
                    }
                    insertString(parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has joined channel " + parser.getTrailing(), doc);
                }
            }
            return;
        }
        if (command.equals("PART")){
            removeFromUserList(parser.getNick());
            insertString(parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has left the server " + parser.getTrailing(), doc);
            return;
        }
        if (command.equals("PING")){
            send("PONG :"+ parser.getTrailing());
            return;
        }
        if (command.equals("PRIVMSG") || command.equals("MSG")){
            insertString((formatNickname("<" + parser.getNick() + ">: ") + parser.getTrailing()).trim(), doc);   
            return;
        }
        if (command.equals("QUIT")){
            //user quits
            removeFromUserList(parser.getNick());
            insertString(parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has left the server " + parser.getTrailing(), doc);
            return;
        }
        if (command.equals("001") || command.equals("002") || command.equals("003") || command.equals("004") || command.equals("005")){
            insertString(parser.getTrailing(), doc);
            return;
        }
	else if(command.equals("251") || command.equals("252") || command.equals("253") || command.equals("254") || command.equals("255") || 
                command.equals("256") || command.equals("257") || command.equals("258") || command.equals("259")){
            insertString(parser.getTrailing(), doc);
            return;
        }
        if (command.equals("265") || command.equals("266")){
            insertString(parser.getTrailing(), doc);
            return;
        }
        if (command.equals("331") || command.equals("332")){
            //  332: Topic, 331: No topic
            if (command.equals("332")) insertString(parser.getTrailing(), doc);
            return;
        }
        if (command.equals("333")){
            //who set topic and the time
            if (command.equals("333")) insertString(parser.getTrailing(), doc);
            return;            
        }
        if (command.equals("353")){
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
            }
        if (command.equals("371") || command.equals("372") || command.equals("374") || command.equals("375") || command.equals("376")){
            insertString(parser.getTrailing(), doc);
            return;
        }
        if (command.equals("471")){
            //unknown command
            insertString(parser.getTrailing(), doc);
        }
        if (command.equals("439")){
            //Message spamming
            insertString(parser.getTrailing(), doc);
            return;
        }
        if (command.equals("451")){
            insertString(parser.getTrailing(), doc);
            return;
        }
        else insertString(line, doc);        
    }
    
    public void run(){
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
