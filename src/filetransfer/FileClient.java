package filetransfer;




/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileClient {

    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.err.println(
                    "Usage: java EchoClient <host name> <port number> <filepath>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        final int size = 8388608;
        
        try (
                Socket serverSocket = new Socket(hostName, portNumber);
                ObjectOutputStream outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(serverSocket.getInputStream());
                BufferedOutputStream bufferedOutput = new BufferedOutputStream(serverSocket.getOutputStream());) {
            
            System.out.println(inputStream.readObject() + serverSocket.getInetAddress().getHostName());
            

            File file = new File(args[2]);
            bufferedOutput.flush();
            if (file.exists()) {
                outputStream.writeObject(file);
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
                long secs = start.until(end, SECONDS);
                long mins = secs > 60 ? secs - start.until(end, MINUTES) * 60 : 0;
                long sizeOfFile = file.length();long oneMb = 1024 * 1024;
                float avSpeed = ((float) sizeOfFile/ oneMb) / (millis < 1000 ? 1 : (millis / 1000));
                
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
                System.out.println(args[2] + "does not exist!");
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to "
                    + hostName);
            System.exit(1);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            System.out.println("Connection terminated");
        }
    }
}
