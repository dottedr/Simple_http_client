package turtleclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read a turtle-graphics encoding of ASCII-art from the file named on the
 * command line and send it to a server to be decoded. Print the decoded result
 * to the console.
 */
public class TurtleClient {

    static final String SERVER_HOST = "www.staff.city.ac.uk";
    static final int SERVER_PORT = 80;
    static final String URI = "/s.hunt/Turtle.php";

    public static void main(String[] args) throws IOException {

        Path relativePath = Paths.get("test-inputsV2/ascii");
        Path absolutePath = relativePath.toAbsolutePath().normalize();

        System.out.println("Welcome to the Simple HTTP Client. \n"
                + "You are currently in" + absolutePath + "\n"
                + "Type the name of the file you would like to POST");

        // read the file contents into a byte array
        //try {
        String fileName = args[0];
        Path path = Paths.get(absolutePath.toString(), fileName);
        System.out.println("You will post: " + path);

        byte[] data = Files.readAllBytes(path);
        //}
        //catch(Exception ex){
        //    System.out.println("Exception occurred");
        //}

        // open a network connection
        try (Socket socket = new Socket(InetAddress.getByName(SERVER_HOST), SERVER_PORT)) {
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();

            Writer writer = new java.io.OutputStreamWriter(os, "US-ASCII");
            // send the POST request (can use the Writer here)
            // send an HTTP request       

            writer.write("POST " + URI + " HTTP/1.1\n");

            // send the necessary HTTP headers (can use the Writer here)
            writer.write("Host: " + SERVER_HOST + "\n");
            writer.write("Content-Length: " + data.length + "\n");

            // send a blank line and flush output to server
            writer.write("\n");
            writer.flush();
            os.flush();

            // send message body (the bytes that we read from the file previously)
            // Use the raw OutputStream here, NOT the Writer.
            // Why? Because we need to send exactly the number of bytes
            // that we advertised in the Content-Length header, without
            // any character encoding messing things up
            os.write(data);

            // flush output to server
            os.flush();

            // Use readHeaderLine(is) to read each response header line in turn.
            //
            // While reading the headers, determine the size in bytes
            // of the response message body (the value of the Content-Length header)
            //
            // The final header line will be empty (everything after that
            // is part of the message body).
            Pattern pattern = Pattern.compile("(Content-Length: )([0-9]{3,5})");
            Matcher m;
            String headerLine;
            while ((headerLine = readHeaderLine(is)) != null && !headerLine.isEmpty()) {
                System.out.println(headerLine);
                m = pattern.matcher(headerLine);
                if (m.find()) {
                    int contentLength = Integer.parseInt(m.group(2));
                    System.out.println(contentLength);
                }
            }
            System.out.println();

            // read the response message body into a byte array
            //
            // TODO
            byte[] turtleGraphics = new byte[897];
            // copied solution from SimpleClient
            int b;
            while ((b = is.read()) != -1) {
                System.out.print((char) b);
                System.out.flush();
            }
            // decode the message bytes and output as text
            // For this exercise you should assume that the
            // message body is unicode text encoded with UTF-8.
            //
            // TODO
        } catch (Exception ex) {
            System.out.println("Something has crashed");
        }

    }

    /**
     * Read an HTTP header line.
     */
    private static String readHeaderLine(InputStream is) throws IOException {
        String line;
        int ch = is.read();
        if (ch == -1) {
            line = null;
        } else {
            line = "";
        }
        while (ch != -1 && ch != '\r') {
            line += (char) ch;
            ch = is.read();
        }
        if (ch == '\r') {
            is.read(); // consume line-feed
        }
        return line;
    }
}
