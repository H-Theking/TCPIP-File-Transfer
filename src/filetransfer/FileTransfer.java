/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransfer;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author harvey
 */
public class FileTransfer {

    private static final Scanner scanner = new Scanner(System.in);
    private static String input;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("\t\t\tUsage\n"
                + "Starting server          -    rcv\n"
                + "Sending file on client   -    ncp source_file dest_file@receivername\n");
        input = scanner.nextLine();
        if (input.equals("rcv")) {
            try {
                System.out.println("Starting server");
                FileServer fileServer = new FileServer();
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (input.split(" ").length != 3) {
            System.out.println("Wrong number of parameters.");
        } else {
            String regex = "ncp (.+) (.+)@(.+)";
            if (!input.matches(regex)) {
                System.out.println("Unrecongized command parameters.");
                return;
            }
            String[] tokens = input.split(" ");
            FileClient client = new FileClient(tokens[2].split("@")[1], tokens[1], tokens[2].split("@")[0]);

        }

    }

}
