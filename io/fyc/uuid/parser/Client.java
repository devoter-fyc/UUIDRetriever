package io.fyc.uuid.parser;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

public class Client {
    public static void main(String[] args) throws Exception{
       var h = new Handler(new Socket(args[0], Integer.parseInt(args[1])), args[2]);
       if (h.run().equals("")) {
              System.out.println("Error has proobably occurred.");
              throw new TestException("Error has proobably occurred.");
       }
       System.out.println("Test done.");
    }
}

class TestException extends RuntimeException {
    public TestException() {
        super();
    }
    
    public TestException(String message) {
        super(message);
    }
}

class Handler {
    protected Socket sock;
    private String someUUID;

    public Handler(Socket sock, String someUUID) {
        this.sock = sock;
        this.someUUID = someUUID;
    }

    public String getSomeUUID() {
        return someUUID;
    }

    public void setSomeUUID(String someUUID) {
        this.someUUID = someUUID;
    }

    public String run() throws IOException{
        try (InputStream input = this.sock.getInputStream()) {
            try (OutputStream output = this.sock.getOutputStream()) {
                return handle(input, output);
            }
        } catch (Exception e) {
            try {
                this.sock.close();
            } catch (IOException q) {
                // DO NOTHING AT ALL
            }
        }
        System.out.println("Client disconnected.");
        return "";
    }

    private String handle(InputStream input, OutputStream output) throws IOException {
        var writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String s = reader.readLine();
        if (!s.equals("Welcome.")) {
            System.err.println("Server is incorrectly setup.");
            throw new SocketException();
        }
        if (this.someUUID.equals("")) {
            System.err.println("UUID is not given. Can't send request.");
            throw new IllegalArgumentException();
        }
        writer.write(this.someUUID);
        writer.flush();
        String response = reader.readLine();
        if (response.equals("File is not found.")) {
            System.err.println("File not found, quitting.");
            throw new FileNotFoundException();
        }
        if (response.equals("JSON format is illegal.")) {
            System.err.println("JSON format is illegal, quitting.");
            throw new RemoteException();
        }
        return response;
    }
}
