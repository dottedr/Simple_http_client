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

        Path relativePath = Paths.get("test-inputsV2/utf-8");
        Path absolutePath = relativePath.toAbsolutePath().normalize();

        System.out.println("Welcome to the Simple HTTP Client");

        // read the file contents into a byte array
        String fileName;
        Path path;
        byte[] data = null;
        
        try {
        fileName = args[0];
        path = Paths.get(absolutePath.toString(), fileName);
        System.out.println("File to be decoded: " + path);
        data = Files.readAllBytes(path);
        }
        
        catch(Exception ex){
            System.out.println("Error while reading a .atl file");
        }

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
            int contentLength = 0;
            while ((headerLine = readHeaderLine(is)) != null && !headerLine.isEmpty()) {                
                m = pattern.matcher(headerLine);

                if (m.find()) {
                    contentLength = Integer.parseInt(m.group(2));
                }
            }
            

            // read the response message body into a byte array
            byte[] turtleGraphics = new byte[contentLength];
            is.read(turtleGraphics);
            // decode the message bytes and output as text
            // For this exercise you should assume that the
            // message body is unicode text encoded with UTF-8.
            String s = new String(turtleGraphics);
            System.out.print(s);
            
            writer.close();
            socket.close();
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
