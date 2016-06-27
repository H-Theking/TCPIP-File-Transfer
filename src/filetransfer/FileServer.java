package filetransfer;



/*
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.net.*;
import java.io.*;
import java.time.LocalTime;
import static java.time.temporal.ChronoUnit.*;
import java.util.Formatter;

public class FileServer {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        if (args.length != 1) {
            System.err.println("Usage: java FileServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        int size = 8388608;//8Mb
        ServerSocket serverSocket = new ServerSocket(portNumber);
        while (true) {
            System.out.println("Waiting for connection...");
            try (
                    Socket clientSocket = serverSocket.accept();
                    BufferedInputStream input = new BufferedInputStream(clientSocket.getInputStream());
                    ObjectInputStream fileIn = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream infoOut = new ObjectOutputStream(clientSocket.getOutputStream());) {

                infoOut.flush();
                long oneMb = 1024 * 1024;
                String message = "Connected to ";

                infoOut.writeObject(message);//Inform client of successful connection
                System.out.println(message + clientSocket.getInetAddress().getHostName());
//                do {//As long as the connection is alive, wait for data

                File fileReceived = (File) fileIn.readObject(); // File abstraction of sent file
                System.out.println("Downloading " + fileReceived.getName());

                LocalTime start = LocalTime.now();
                try (BufferedOutputStream fileStream = new BufferedOutputStream(new FileOutputStream(fileReceived.getName()))) {
                    byte[] bytes = new byte[size];
                    int read;

                    while ((read = input.read(bytes)) != -1) {
                        fileStream.write(bytes, 0, read);
                    }
                }
                LocalTime end = LocalTime.now();
                long millis = start.until(end, MILLIS);
                long secs = start.until(end, SECONDS);
                long mins = secs > 60 ? secs - start.until(end, MINUTES) * 60 : 0;
                long sizeOfFileRecived = (new File(fileReceived.getName())).length();

                float avSpeed = ((float) sizeOfFileRecived / oneMb) / (millis < 1000 ? 1 : (millis / 1000));
                Formatter format = new Formatter();
                format.format("Download Completed in\t%dm:%ds\n"
                        + "File Size\t\t%.2fMb\n"
                        + "Average Download Speed\t%.1fMbits/sec\n",
                        mins, secs,
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
