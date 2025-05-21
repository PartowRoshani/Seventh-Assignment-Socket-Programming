package Server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    // TODO: Declare a variable to hold the input stream from the socket
    // TODO: Declare a variable to hold the output stream from the socket
    private BufferedReader reader;
    private BufferedWriter writer;
    private List<ClientHandler> allClients;
    private String username;



    public ClientHandler(Socket clientSocket) {
        // TODO: Modify the constructor as needed
        this.socket =clientSocket;
        this.allClients =Server.clients;

    }

    @Override
    public void run() {
        try {

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (true) {
                // TODO: Read incoming message from the input stream
                // TODO: Process the message
                String message = reader.readLine();
                if(message == null)
                {
                    break;
                }

                if(message.startsWith("LOGIN:"))
                {
                    String[] parts = message.split(":");
                    handleLogin(parts[1], parts[2]);
                }
                else if (message.startsWith("SEND:"))
                {
                    broadcast(username + ": " + message.substring(5));
                }

                else if (message.startsWith("DOWNLOAD_REQUEST"))
                {
                    sendFileList();
                }
                else if (message.startsWith("DOWNLOAD_FILE"))
                {
                    String fileName = message.substring("DOWNLOAD_FILE".length()).trim();
                    sendFile(fileName);
                }
                else if (message.startsWith("UPLOAD:"))
                {
                    String[] parts = message.split(":");
                    String filename = parts[1];
                    int size = Integer.parseInt(parts[2]);
                    receiveFile(filename, size);
                }
                else if (message.equalsIgnoreCase("EXIT")) {
                    broadcast(username + " has left the chat.");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Client "+username+" disconnected");

        }
        finally
        {
            //TODO: Update the clients list in Server
            allClients.remove(this);
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }


    private void sendMessage(String msg){
        //TODO: send the message (chat) to the client
        try{
            writer.write(msg);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message "+username);
        }
    }
    private void broadcast(String msg) throws IOException {
        //TODO: send the message to every other user currently in the chat room
        for(ClientHandler client: allClients)
        {
            if(client != this)
            {
                client.sendMessage(msg);
            }
        }
    }

    private void sendFileList(){
        // TODO: List all files in the server directory
        // TODO: Send a message containing file names as a comma-separated string
        File dir = new File("resources/Server/Files");
        File[] files = dir.listFiles();
        if (files == null || files.length == 0)
        {
            sendMessage("0");
            return;
        }
        sendMessage(String.valueOf(files.length));
        for (File file : files) {
            sendMessage(file.getName());
        }
    }
    private void sendFile(String fileName){
        // TODO: Send file name and size to client
        // TODO: Send file content as raw bytes
        try {
            File file = new File("resources/Server/Files/" + fileName);
            if (!file.exists()) {
                sendMessage("-1");
                return;
            }
            sendMessage(String.valueOf(file.length()));

            byte[] buffer = new byte[4096];
            int bytesRead;

            OutputStream out = socket.getOutputStream();
            BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file));

            while ((bytesRead = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            out.flush();
            fileIn.close();
        } catch (IOException e) {
            System.out.println("Error sending file: " + fileName);
        }
    }
    private void receiveFile(String filename, int fileLength)
    {
        // TODO: Receive uploaded file content and store it in a byte array
        // TODO: after the upload is done, save it using saveUploadedFile
        try {
            byte[] data = new byte[fileLength];
            InputStream in = socket.getInputStream();

            int bytesRead = 0;
            while (bytesRead < fileLength)
            {
                int result = in.read(data, bytesRead, fileLength - bytesRead);
                if (result == -1) break;
                bytesRead += result;
            }

            saveUploadedFile(filename, data);
            sendMessage("UPLOAD_SUCCESS");
        }
        catch (IOException e)
        {
            sendMessage("ERROR:Upload failed");
        }
    }
    private void saveUploadedFile(String filename, byte[] data) throws IOException {
        // TODO: Save the byte array to a file in the Server's resources folder
        File dir = new File("resources/Server/Files");
        if (!dir.exists()) dir.mkdirs();

        FileOutputStream fos = new FileOutputStream(new File(dir, filename));
        fos.write(data);
        fos.close();
    }

    private void handleLogin(String username, String password) throws IOException, ClassNotFoundException {
        // TODO: Call Server.authenticate(username, password) to check credentials
        // TODO: Send success or failure response to the client

        if(Server.authenticate(username, password))
        {
            this.username =username;
            sendMessage("LOGIN_SUCCESS");
        }
        else {
            sendMessage("LOGIN_FAIL");
            socket.close();
            return;
        }
    }

}
