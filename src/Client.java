import java.net.*;
import java.io.*;

/*
 * The Client that can be run both as a console or a GUI
 */
public class Client  {

	// I/O with the Server
	private ObjectInputStream sInput;	// socket reader
	private ObjectOutputStream sOutput;	// socket writer
	private Socket socket;

	// GUI object
	private ClientGUI cg;
	
	// the server, the port and the username
	private String server, username, password;
	private int port;

	/*
	 *  Constructor called by console mode
	 *  server: the server address
	 *  port: the port number
	 *  username: the username
	 */
	Client(String server, int port, String username, String password) {
		// which calls the common constructor with the GUI set to null
		this(server, port, username, password, null);
	}

	/*
	 * Constructor call when used from a GUI
	 * in console mode the ClienGUI parameter is null
	 */
	Client(String server, int port, String username, String password, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.password = password;
		// save if we are in GUI mode or not
		this.cg = cg;
	}
	
	/*
	 * To start the dialog
	 */
	public boolean start() {
            // try to connect to the server
            try {
                    socket = new Socket(server, port);
            } 
            // if it failed not much I can so
            catch(Exception ec) {
                    display("Error connecting to server:" + ec);
                    return false;
            }

            String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
            display(msg);

            /* Creating both Data Stream */
            try
            {
                sInput  = new ObjectInputStream(socket.getInputStream());
                sOutput = new ObjectOutputStream(socket.getOutputStream());
            }
            catch (IOException eIO) {
                display("Exception creating new Input/output Streams: " + eIO);
                disconnect();
                return false;
            }

            // creates the Thread to listen from the server 
            new ListenFromServer().start();
            // Send our username to the server this is the only message that we
            // will send as a String. All other messages will be ChatMessage objects
            try
            {
                sOutput.writeObject(username + ":" + password);
            }
            catch (IOException eIO) {
                display("Exception doing login : " + eIO);
                disconnect();
                return false;
            }
            // success we inform the caller that it worked
            return true;
	}

	/*
	 * To send a message to the GUI
	 */
	private void display(String msg)
        {
            if(cg != null)
                cg.append(msg + "\n");  // append to the ClientGUI JTextArea
	}
	
	/*
	 * To send a message to the server
	 */
	void sendMessage(ChatMessage msg) {
            try 
            {
                sOutput.writeObject(msg);
            }
            catch(IOException e) 
            {
                display("Exception writing to server: " + e);
            }
	}

    /*
     * When client disconnects, close input/output steams and socket.
     */
    private void disconnect () 
    {
        // Close input stream
        try 
        { 
            if(sInput != null) 
                sInput.close();
        }
        catch(IOException e) 
        {
            System.out.println("Input stream unable to close upon disconnect");
        } 
        
        // Close output stream
        try 
        {
            if(sOutput != null) 
                sOutput.close();
        }
        catch(IOException e) 
        {
            System.out.println("Output stream unable to close upon disconnect");
        }
        
        // Close socket
        try
        {
            if(socket != null) 
                socket.close();
        }
        catch(IOException e) 
        {
            System.out.println("Socket unable to close upon disconnect");
        } 

        // Alert the ClientGUI
        cg.connectionFailed();
    }

    /*
     * a class that waits for the message from the server and append them to the JTextArea
     * if we have a GUI or simply System.out.println() it in console mode
     */
    class ListenFromServer extends Thread {

        public void run() 
        {
            while(true) 
            {
                try 
                {
                    String msg = (String) sInput.readObject();

                    if (msg.equals("denied"))
                    {
                        display("Incorrect Username/Password");
                        disconnect();
                    }
                    else
                    {
                        display(msg);
                    }
                }
                catch(IOException e) 
                {
                    display("Client has close the connection.");
                    cg.connectionFailed();  // Alert ClientGUI
                    break;
                }
                catch(ClassNotFoundException ex) 
                {
                }
            }
        }
    }
}




