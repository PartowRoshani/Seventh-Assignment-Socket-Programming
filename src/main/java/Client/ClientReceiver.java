package Client;


import java.io.BufferedReader;

public class ClientReceiver implements Runnable {
    private final BufferedReader in;

    // TODO: Declare a variable to hold the input stream from the socket
    public ClientReceiver(BufferedReader in) {
        this.in = in;
        // TODO: Modify this constructor to receive either a Socket or an InputStream as a parameter
        // TODO: Initialize the input stream variable using the received parameter
    }


    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null)  {
                //TODO: Listen for new messages from server
                //TODO: print the  new message in CLI
                if (msg.equals("CHAT_END")) break;
                System.out.println("[Chat] " + msg);
            }
        } catch (Exception e) {
            System.out.println("Error receiving chat messages: " + e.getMessage());
        }
    }

}



