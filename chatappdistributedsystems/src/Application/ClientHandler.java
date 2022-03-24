package Application;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientName;

    public ClientHandler(Socket socket){
        try {
            this.socket=socket;
            this.bufferedWriter =new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader =new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientName = bufferedReader.readLine();
            clientHandlers.add(this);
           // sendMessage("SERVER: "+clientName+ " has connected");

        }catch (IOException e){
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }


    @Override
    public void run() {
        String messagefromClient;
        while (socket.isConnected()){
          try {
              messagefromClient = bufferedReader.readLine();
             // sendMessage(messagefromClient);
              if (messagefromClient.equals("logout")){
                  closeAll(socket,bufferedReader,bufferedWriter);


                  break;
              }
              if (messagefromClient.startsWith("P")){
                  String[] messageportion = messagefromClient.split("%");
                  String sendingto = messageportion[1];
                  StringBuilder privMessage = new StringBuilder("Private message from "+ clientName + ":");
                  for (int i = 2; i <messageportion.length;i++){
                      privMessage.append(messageportion[i]).append(" ");
                  }
                  privMessage.append("\n");
                  secretMessage(sendingto,privMessage.toString());

              }
              else {
                  String message = clientName + " : "+ messagefromClient;
                  sendMessage(message);
              }

          } catch (IOException e) {
              closeAll(socket, bufferedReader, bufferedWriter);
              break;
          }

        }

    }
    public  void sendMessage(String theMessage){
        for (ClientHandler clientHandler : clientHandlers){
            try {
                if(!clientHandler.clientName.equals(clientName)){
                    clientHandler.bufferedWriter.write(theMessage);
                    System.out.println(theMessage);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeAll(socket, bufferedReader, bufferedWriter);
            }
        }
    }
    public void removeCLientHandler(){
        clientHandlers.remove(this);
        sendMessage("SERVER: "+clientName+" has disconnected");
    }
    public void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){

        removeCLientHandler();
        try {
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter!= null){
                bufferedWriter.close();
            }
            if(socket !=null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void secretMessage(String sendto, String themessage){
        for (ClientHandler clientHandler: clientHandlers){
            if(clientHandler.clientName.equals(sendto)){
                try {
                    clientHandler.bufferedWriter.write(themessage);
                    clientHandler.bufferedWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    closeAll(socket,bufferedReader,bufferedWriter);
                }
            }
        }
    }


}
