/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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
//داخل دالة run في NewClient.java

@Override
public void run() {
    try {
        while (true) {

            String request = in.readLine();
            if (request == null) break;

            if (request.startsWith("CONNECT:")) {

                String newName = request.substring(8).trim();

                if (Server.isNameTaken(newName)) {
                    sendMessage("ERROR:Name already taken");
                } else {
                    this.playerName = newName;
                    Server.AddConnectedPlayer(playerName);
                }
            }

            else if (request.startsWith("Play:")) {

                if (playerName != null) {
                    Server.AddToWaitingRoom(playerName);
                }
            }
        }

    } catch (IOException e) {
        System.out.println("Client disconnected");
    } finally {
        closeResources();
    }
}
private void closeResources() {
 try {
     if (out != null) out.close();
     if (in != null) in.close();
     if (client != null) client.close();
     
     // تنظيف القوائم عند الخروج (اختياري حسب منطق مشروعك)
     // Server.removePlayer(playerName); 
     
     System.out.println("Resources closed for: " + playerName);
 } catch (IOException e) {
     e.printStackTrace();
 }
}
public void  sendMessage(String message){
out.println(message);
    
}
private void outToAll(String substring) {
for (NewClient aclient:clients) {
aclient.out.println(substring);
}
}


}
