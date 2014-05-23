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
    
    public void run(){
            
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
                writer.flush();
                while ((line = reader.readLine()) != null){
                    line = line.substring(line.indexOf(" ")+1);
                    if (line.startsWith("NOTICE AUTH :")) line = line.substring(13);
                    if (line.contains(nick)) line = line.substring(line.indexOf(nick));
                    
                    try{
                        if (line.startsWith("PING ")){
                            writer.write("PONG " + line.substring(5) + "\r\n");
                            writer.flush();
                            continue;
                        }
                        if (line.startsWith("JOIN :")){
                            channel = line.substring(line.indexOf("#"));
                            System.out.println("CHANNEL "+channel);
                        }
                        if (!channel.equals("") && line.startsWith(nick+ " = "+channel)){
                            if (userList.getLength() > 0) userList.remove(0, userList.getLength()); //large  channels send users in more than one line FIX
                            String[] nn = line.split(" ");
                            Arrays.sort(nn);
                            for (int i = 3; i < nn.length; i++){
                                userList.insertString(userList.getLength(), nn[i]+"\n", null);
                            }
                            System.out.println(line);
                            
                        }
                        else{
                            doc.insertString(doc.getLength(), line+"\n", null);
                            System.out.println(line+"\n");
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
    }

}
