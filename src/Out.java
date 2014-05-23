import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class Out extends Thread{
    Socket socket;
    
    public Out(Socket socket){
        this.socket = socket;
    }
    
    public void run(){
        try
      {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream( )));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream( )));
        //writer.flush();
        String line = null;
        while ((socket.isConnected())){
            line = reader.readLine();
            if (line == null) continue;
            //if ((line.indexOf("004")) >= 0) break;
            if (line.indexOf("433") >= 0){
                System.out.println("Nick in use");
                //return;
            }
            if (line.startsWith("PING ")){
                writer.write("PONG " + line.substring(5) + "\r\n");
                writer.flush();
            }
            System.out.println(line); 
        }     
        System.out.println("  Connection broken");
        
      }catch(IOException e)
      {
         e.printStackTrace();
      }
    }
    
}
