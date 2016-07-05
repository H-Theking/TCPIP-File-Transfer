package filetransfer;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
//import java.net.*;
import java.time.LocalTime;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileClient {

    private final int portNumber = 5555;
    private final int size = 8388608;
    private final long oneMb = 1024 * 1024;

    public FileClient(String hostName, String source, String dest) {
        Socket serverSocket = null;
        try {
            serverSocket = new Socket(hostName, portNumber);
            serverSocket.setSoTimeout(10 * 1000);
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error occured while trying to get socket");
            System.exit(1);
        }
        try (
                ObjectOutputStream outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(serverSocket.getInputStream());
                BufferedOutputStream bufferedOutput = new BufferedOutputStream(serverSocket.getOutputStream());) {

            System.out.println(inputStream.readObject() + serverSocket.getInetAddress().getHostName());

            File file = new File(source);
            bufferedOutput.flush();
            if (file.exists()) {
                outputStream.writeObject(file);
                outputStream.writeObject(dest);

                System.out.println("Uploading " + file.getName());
                LocalTime start = LocalTime.now();
                FileInputStream fileInput = new FileInputStream(file);
                byte[] bytes = new byte[size];
                int read;
                while ((read = fileInput.read(bytes)) != -1) {
                    bufferedOutput.write(bytes, 0, read);
                }
                LocalTime end = LocalTime.now();
                long millis = start.until(end, MILLIS);
                long mins = start.until(end, MINUTES);
                long secs = start.until(end, SECONDS) - mins * 60;
                long sizeOfFile = file.length();
                float avSpeed = ((float) sizeOfFile / oneMb) / (millis < 1000 ? 1 : (millis / 1000));

                Formatter format = new Formatter();
                format.format("Upload Completed in\t%dm:%ds\n"
                        + "File Size\t\t%.2fMb\n"
                        + "Average Upload Speed\t%.1fMbits/sec\n",
                        mins, secs,
                        (float) sizeOfFile / oneMb,
                        avSpeed);
                System.out.println(format.toString());
//                inputStream.readObject();
            } else {
                System.out.println(source + "does not exist!");
            }
        }  catch (IOException e) {
            if (e.getClass().equals(SocketTimeoutException.class)) {
                System.out.println("Server is busy.");
            }
            System.err.println("Couldn't get I/O for the connection to "
                    + hostName);
            System.exit(1);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("Connection terminated");
        }
    }
}
