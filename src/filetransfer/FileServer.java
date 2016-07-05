package filetransfer;

import java.net.*;
import java.io.*;
import java.time.LocalTime;
import static java.time.temporal.ChronoUnit.*;
import java.util.Formatter;

public class FileServer {

    private final ServerSocket serverSocket;
    private final int portNumber = 5555;
    private final int size = 8388608;//8Mb
    private final long oneMb = 1024 * 1024;

    public FileServer() throws IOException, ClassNotFoundException {

        serverSocket = new ServerSocket(portNumber);

        while (true) {
            System.out.println("Waiting for connection...");
            try (
                    Socket clientSocket = serverSocket.accept();
                    BufferedInputStream input = new BufferedInputStream(clientSocket.getInputStream());
                    ObjectInputStream fileIn = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream infoOut = new ObjectOutputStream(clientSocket.getOutputStream());) {

                infoOut.flush();

                String message = "Connected to ";

                infoOut.writeObject(message);//Inform client of successful connection
                System.out.println(message + clientSocket.getInetAddress().getHostName());
//                do {//As long as the connection is alive, wait for data

                File fileReceived = (File) fileIn.readObject(); // File abstraction of sent file
                System.out.println("Downloading " + fileReceived.getName());

                String dest = (String) fileIn.readObject();

                LocalTime start = LocalTime.now();
                try (BufferedOutputStream fileStream = new BufferedOutputStream(new FileOutputStream(dest))) {
                    byte[] bytes = new byte[size];
                    int read;

                    while ((read = input.read(bytes)) != -1) {
                        fileStream.write(bytes, 0, read);
                    }
                }
                LocalTime end = LocalTime.now();
                long millis = start.until(end, MILLIS);
                long mins = start.until(end, MINUTES);
                long secs = start.until(end, SECONDS) - mins*60;
                long sizeOfFileRecived = (new File(dest)).length();

                float avSpeed = ((float) sizeOfFileRecived / oneMb) / (millis < 1000 ? 1 : (millis / 1000));
                Formatter format = new Formatter();
                format.format("Download Completed in\t%dm:%ds\n"
                        + "Saving file as\t%s\n"
                        + "File Size\t\t%.2fMb\n"
                        + "Average Download Speed\t%.1fMbits/sec\n",
                        mins, secs, dest,
                        (float) sizeOfFileRecived / oneMb,
                        avSpeed);
                System.out.print(format.toString());
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port "
                        + portNumber + " or listening for a connection");
                System.out.println(e.getMessage());
            } finally {
                System.out.println("Connection terminated\n");

            }
        }
    }
}
