import java.io.IOException;
import java.net.SocketException;
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


public class Connection implements Runnable{
    Thread thread;
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    FileWriter chatLog;
    String server, host, nick = "rieux", channel = "";
    int port;
    DefaultStyledDocument doc, userList;
    public ArrayList<String> users;
    
    public Connection(String server, int port, DefaultStyledDocument doc, DefaultStyledDocument userList){
       this.server = server;
       this.port = port;
       this.doc = doc;
       this.userList = userList;
       
       thread = new Thread(this);
       thread.start();
    }     
    public void insertString(String line, DefaultStyledDocument target) throws BadLocationException{
        target.insertString(target.getLength(), line+"\n", null);
    }
    public void send(String line) throws IOException{
        try{ 
            writer.write(line+"\r\n");
        } catch (IOException e){
            //do nothing
        }
        
    }

    public void parseFromServer(String line) throws IOException, BadLocationException{
        
        Parser parser = new Parser(line);
        String command = parser.getCommand();
        
        if (command.equals("PING")) this.send("PONG :"+ parser.getTrailing());
        if (command.equals("JOIN")){
            if (nick.equals(parser.getNick())){
                String channelName = parser.getTrailing();
                if (channelName.startsWith("#")){
                    channelName = channelName.substring(1);
                    insertString(parser.getNick()+" (" + parser.getUser() + "@" + parser.getHost() + ") has joined channel " + parser.getTrailing(), doc);
                }
            }
        }
        if (command.equals("PRIVMSG") || command.equals("MSG")){
            String destination = parser.getMiddle();
            if (destination.startsWith("#")){
                String channelName = destination.substring(1);
                insertString()
            }
        }
        
    }
    
    
    
    
    
    
    
    
    public void run(){
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        /*ArrayList<String> list = new ArrayList<String>();
        try{
            socket = new Socket(server, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            if (socket != null){
                //TODO notify of connectivity
            }
        } catch (Exception ex){
            //TODO notify of connection failure
            return;
        }   
        String line = null;
        try{
            while (socket.isConnected()){
                writer.write("NICK "+ nick+"\r\n");
                writer.write("USER "+nick+" 8 * : a bot\r\n");
                writer.write("join #lmitb\r\n");
                writer.flush();
                while ((line = reader.readLine()) != null){
                   System.out.println(line);
                   if (line.startsWith("PING ")){
                       line = line.substring(line.indexOf(" ")+1);
                       writer.write("Pong " + line.substring(5) + "\n");
                       writer.flush();
                       continue; 
                   }
                   
                   if (line.length() > 10 && line.substring(0,10).contains("!") && line.contains("PRIVMSG")){
                       System.out.println("######"+line);
                       String nick = line.substring(0,line.indexOf("!"));
                       line = line.substring(line.indexOf("#"));
                       String channel = line.substring(0,line.indexOf(" "));
                       System.out.println(nick+ "       "+channel);
                   }
                   
                   
                   
                   line = line.substring(line.indexOf(" "));
                   
                    System.out.println(line);
                    try{
                       // if (line.substring(0,10).contains("!")){
                         //   System.out.println(line.substring(0,10));                            
                       // }
                        if (line.startsWith("372") || line.startsWith("375") || line.startsWith("376")){
                            insertString(line.substring(line.indexOf(nick)+nick.length()+1),doc);
                            continue;
                        }
                       // if (line.startsWith("PING ")){
                         //   //System.out.println(line);
                           // writer.write("PONG " + line.substring(5) + "\n");
                            //writer.flush();
                            //continue;
                        //}
                        if (line.startsWith("JOIN :")){
                            channel = line.substring(line.indexOf("#"));
                            continue;
                        }
                        if (line.startsWith("353 "+nick+ " * "+channel) || line.startsWith("353 "+nick+ " = "+channel)){
                            line = line.substring(line.indexOf(":")+1);
                            //System.out.println(line);
                            String[] nn = line.split(" ");
                            list.addAll(Arrays.asList(nn));
                            continue;
                          
                        }
                        if (line.equals("366 "+nick+" "+channel+" :End of /NAMES list.")){
                            SortedSet<String> set = new TreeSet(String.CASE_INSENSITIVE_ORDER);
                            userList.remove(0, userList.getLength());
                            set.addAll(list);
                            Iterator<String> iterator = set.iterator();
                            while (iterator.hasNext()){
                                String nextElement = iterator.next();
                                insertString(nextElement, userList);
                            }
                            continue;
                        }
                        else{
                            insertString(line, doc);
                            //System.out.println(line+"\n");
                        }
                    } catch (BadLocationException e){
                        //do nothing
                    }
                    if (line.startsWith("ERR")){
                        System.exit(1);
                    }
                }   
             }
        } catch (SocketException ex){
            System.out.println("socket closed");
        } catch (IOException ex){
            System.out.println("ioexception occured");
        }
    }*/

}
