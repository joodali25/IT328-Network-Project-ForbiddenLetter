/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package networkProject;

/**
 *
 * @author juman
 */
 
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
public class server
{
private static ArrayList<NewClient> clients=new ArrayList<>();
private static ArrayList<String> connectedPlayers = new ArrayList<>();
private static ArrayList<String> waitingRoom = new ArrayList<>();
public static void main(String[] args) throws IOException
{
ServerSocket serverSocket = new ServerSocket(9090);
while (true){
System.out.println("Waiting for client connection");
Socket client=serverSocket.accept();
System.out.println("Connected to client");
NewClient clientThread=new NewClient(client,clients); // new thread
clients.add(clientThread);

new Thread (clientThread).start();
}
}
//add new player to witig room
 public static void addTOWitngRoom(String playrname){
   waitingRoom.add(playrname);
  System.out.println(playrname + " joined waiting room" );
   updateAllClients();
}

 //add new connected player
  public static void AddconnectedPlayers (String playrname){
    connectedPlayers.add( playrname);
    System.out.println("CONNECTED:" + playrname);
    updateAllClients();
  }
   
public static void updateAllClients() {
    String players = "WAITING:" + String.join(",", waitingRoom);
    String connected = "PLAYERS:" + String.join(",", connectedPlayers);
    
    System.out.println("   " + players);
    System.out.println("   " + connected);
    
    for(NewClient client : clients) {
        client.sendmessage(connected);
        client.sendmessage(players);
    }
}

}

