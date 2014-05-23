import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.lang.Thread;
import java.util.logging.Level;
import java.util.logging.Logger;

public class In extends Thread{
    
    Socket socket;
    
    public In(Socket socket){
        this.socket = socket;
    }    
      
      public void sleep(int timeMS){
          try{
              Thread.sleep(timeMS);
          }catch (InterruptedException e){
              System.out.println("interrupted exception");
          }
      }
      
    //Thread
    public void run(){
        BufferedReader reader = null; //necessary?
        Scanner keyboard = new Scanner(System.in);
        String channel = "";
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream( )));
            String usrinput = "";
            writer.write("NICK torsoe2\r\n");
            writer.write("USER torsoe2 8 * : a bot\r\n");
            writer.flush();
            
            while (socket.isConnected()){
                usrinput = keyboard.nextLine();
                if (!channel.equals("") && usrinput.startsWith("/")){
                    writer.write(usrinput.substring(1)+"\r\n");
                    writer.flush();
                    continue;
                }
                if (usrinput.toLowerCase().startsWith("join #")){ //doesnt allow channel switching
                    channel = "PRIVMSG "+usrinput.substring(usrinput.indexOf('#'))+ " :";
                    writer.write(usrinput+"\r\n");
                    writer.flush();
                    continue;
                }
                writer.write(channel+usrinput+"\r\n"); 
                writer.flush();
            }     
        } catch (IOException ex) {
            Logger.getLogger(In.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(In.class.getName()).log(Level.SEVERE, null, ex);
            }
        }     
      }
   
}