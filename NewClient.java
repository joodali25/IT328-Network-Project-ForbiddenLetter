/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package networkProject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
class NewClient implements Runnable
{
private Socket client;
private BufferedReader in;
private PrintWriter out;
private String playerName;
private boolean isConnected;
private ArrayList<NewClient> clients;
public NewClient (Socket c,ArrayList<NewClient> clients) throws IOException
{
this.client = c;
this.clients=clients;
in= new BufferedReader (new InputStreamReader(client.getInputStream()));
out=new PrintWriter(client.getOutputStream(),true);
}
@Override
public void run ()
{
try{
while (true){
String request=in.readLine();
if (playerName != null) {
    System.out.println("Received from " + playerName + ": " + request);
} else {
    System.out.println("Received from Unknown: " + request);
}
//check if he is plyer or just connect
if(request.startsWith("CONNECT:")){
String NewplayerName=request.substring(8);
//check plyer name
if (clients.contains( NewplayerName)){
   sendmessage("ERROR:Name already taken") ;
 continue;
}
 this.playerName = NewplayerName;
}   
//add plyer name to connected 
 server.AddconnectedPlayers (playerName);
 
if(request.startsWith("Play:"))
     if (playerName == null) {
        sendmessage("ERROR:You must connect first");
         continue;
                    }
  server.addTOWitngRoom(playerName);
}
} catch (IOException e){
System.err.println("IO exception in new client class");
System.err.println(e.getStackTrace());
} finally{
       
out.close();
try {
in.close();
} catch (IOException ex) {
ex.printStackTrace();
}
}
}
public void  sendmessage(String message){
out.println(message);
    
}
private void outToAll(String substring) {
for (NewClient aclient:clients) {
aclient.out.println(substring);
}
}


}
