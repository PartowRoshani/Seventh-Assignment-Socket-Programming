package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    // TODO: Declare variables for socket input/output streams
    private static String username;
    private static PrintWriter out;
    private static BufferedReader in;
    private static InputStream inputStream;
    private static OutputStream outputStream;


    public static void main(String[] args) throws Exception {

        try (Socket socket = new Socket("localhost", 12345)) {
            //TODO: Use the socket input and output streams as needed
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            Scanner scanner = new Scanner(System.in);

            // --- LOGIN PHASE ---
            System.out.println("===== Welcome to CS Music Room =====");


            boolean loggedIn = false;
            while (!loggedIn) {
                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();


                sendLoginRequest(username, password);

                // TODO: Receive and check the server's login response
                // TODO: Set 'loggedIn = true' if credentials are correct; otherwise, prompt again

                String response = in.readLine();
                if ("LOGIN_SUCCESS".equals(response)) {
                    System.out.println("Login successful!");
                    loggedIn = true;
                } else {
                    System.out.println("Login failed. Try again.");
                }
            }

            // --- ACTION MENU LOOP ---
            while (true) {
                printMenu();
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> enterChat(scanner);
                    case "2" -> uploadFile(scanner);
                    case "3" -> requestDownload(scanner);
                    case "0" -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Enter chat box");
        System.out.println("2. Upload a file");
        System.out.println("3. Download a file");
        System.out.println("0. Exit");
    }

    private static void sendLoginRequest(String username, String password) {
        //TODO: send the login request
        out.println("LOGIN:" + username + ":" + password);
    }
    private static void enterChat(Scanner scanner) throws IOException {
        System.out.print("You have entered the chat ");
        System.out.println(">");


        Thread resiverThread = new Thread(new ClientReceiver(in));
        resiverThread.start();
        //TODO: Create and start ClientReceiver thread to continuously get new messages from server
        String message_string = "";
        while (true) {
            message_string = scanner.nextLine();

            if (message_string.equalsIgnoreCase("/exit")) {
                out.println("EXIT");
                out.flush();
                break;
            } else {

                out.println("SEND:" + message_string);
                out.flush();
            }
        }
    }

    private static void sendChatMessage(String message_to_send) throws IOException {
        //TODO: send the chat message
        out.println(message_to_send);
    }

    private static void uploadFile(Scanner scanner) throws IOException {

        //TODO: list all files in the resources/Client/<username> folder
        File userDirectory = new File("C:\\Users\\User\\Desktop\\AP\\Seventh-Assignment-Socket-Programming\\src\\main\\resources\\Client\\" + username);
        File[] files = userDirectory.listFiles((dir, name) -> new File(dir, name).isFile());

        if (files == null || files.length == 0) {
            System.out.println("No files to upload.");
            return;
        }

        // Show available files
        System.out.println("Select a file to upload:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }

        System.out.print("Enter file number: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice < 0 || choice >= files.length) {
            System.out.println("Invalid choice.");
            return;
        }

        File file = files[choice];
        out.println("UPLOAD:" + file.getName() + ":" + file.length());
        // TODO: Notify the server that a file upload is starting (e.g., send file metadata)
        // TODO: Read the file into a byte array and send it over the socket
        try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
        System.out.println("File uploaded successfully.");
    }

    private static void requestDownload(Scanner scanner) throws IOException {
        // TODO: Send a request to the server to retrieve the list of available files
        out.println("DOWNLOAD_REQUEST");
        int fileCount = Integer.parseInt(in.readLine());
        if (fileCount == 0)
        {
            System.out.println("No files available for download.");
            return;
        }
        // TODO: Display the file names and prompt the user to select one
        String[] fileNames = new String[fileCount];
        System.out.println("Available files:");
        for (int i = 0; i < fileCount; i++)
        {
            fileNames[i] = in.readLine();
            System.out.println((i + 1) + ". " + fileNames[i]);
        }

        System.out.print("Enter file number to download: ");
        int choice;
        try{
            choice = Integer.parseInt(scanner.nextLine())-1;
        }
        catch (NumberFormatException e)
        {
            System.out.println("Invalid input.");
            return;
        }
        if (choice < 0 || choice >= fileCount)
        {
            System.out.println("Invalid file selection.");
            return;
        }
        out.println("DOWNLOAD_FILE "+fileNames[choice]);

        long fileSize = Long.parseLong(in.readLine());
        File userDir = new File("C:\\Users\\User\\Desktop\\AP\\Seventh-Assignment-Socket-Programming\\src\\main\\resources\\Client\\" + username);
        if (!userDir.exists())
        {
            userDir.mkdirs();
        }

        File fileToSave = new File(userDir, fileNames[choice]);
        // TODO: Download the selected file and save it to the user's folder in 'resources/Client/<username>'

        try (FileOutputStream fileOut = new FileOutputStream(fileToSave))
        {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;

            while (totalRead < fileSize && (bytesRead = inputStream.read(buffer)) != -1)
            {
                fileOut.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }
            fileOut.flush();
        }

        System.out.println("File downloaded successfully to " + fileToSave.getPath());
    }
}
