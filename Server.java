import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;
	// if I am in a GUI
	private ServerGUI sg;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean online;
	// list of chat rooms
	private ArrayList<ChatRoom> rooms;
	// the number of rooms currently in existence
	private static int numberOfRooms = 0;
	
	/*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 */
	public Server(int port) {
		this(port, null);
	}
	
	public Server(int port, ServerGUI sg) {
		// GUI or not
		this.sg = sg;
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		al = new ArrayList<ClientThread>();
		rooms = new ArrayList<ChatRoom>();
	}
	
	public void start() {
		online = true;
		/* create socket server and wait for connection requests */
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// As long as server is online, it will continue to listen for incoming connections.
			while(online) 
			{
				display("Server listening on port " + port);
				
				// Blocks until a incoming connection is made.
				Socket socket = serverSocket.accept();		

				// if I was asked to stop
				if(!online)
					break;
				ClientThread t = new ClientThread(socket);  // make a thread of it
				al.add(t);									// save it in the ArrayList
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();


				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						// not much I can do
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		
    /*
     * For the GUI to stop the server
     */
	protected void stop() {
		online = false;
		// connect to myself as Client to exit statement 
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			// nothing I can really do
		}
	}
	/*
	 * Display an event (not a message) to the console or the GUI
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if(sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}
	/*
	 *  to broadcast a message to all Clients
	 */
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";
		// display message on console or GUI
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);     // append in the room window
		
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(messageLf)) {
				al.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}
	
	private synchronized void broadcastToRooms(String message, ClientThread c) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";
		ArrayList<ClientThread> members = new ArrayList<ClientThread>();
		// display message on console or GUI
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);     // append in the room window
		// 
		for(int i = 0 ; i < rooms.size(); i++)
		{
			ChatRoom temp = rooms.get(i);
			
			if(temp.isMember(c))
			{
				temp.broadcastRooms(message);
			}
				// try to write to the Client if it fails remove it from the list
//					if(!ct.writeMsg(messageLf)) {
//					al.remove(i);
//						display("Disconnected Client " + ct.username + " removed from list.");
//					}
		}
	}

	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}
	
	/*
	 *  To run as a console application just open a console window and: 
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */ 
	public static void main(String[] args) {
		// start server on port 1500 unless a PortNumber is specified 
		int portNumber = 1500;
		// Connect to the DB
		
		// Load JDBC Driver for MySQL
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("JDBC driver not found");
		}
		
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}
	
	class ChatRoom
	{
		private String name = "";
		//int number;
		public ArrayList<ClientThread> roomMembers = new ArrayList<ClientThread>();
		
		ChatRoom(String name)
		{
			this.name = name;
			//this.number = num;
		}
		ChatRoom(ChatRoom c)
		{
			this.name = c.name;
			this.roomMembers = c.roomMembers;
			//this.number = c.number;
		}
		
		void broadcastRooms(String m)
		{
			for(int i = roomMembers.size(); --i >= 0;) {
				ClientThread ct = roomMembers.get(i);
				// try to write to the Client if it fails remove it from the list
				if(!ct.writeMsg(m)) {
					roomMembers.remove(i);
					//display("Disconnected Client " + ct.username + " removed from list.");
				}
			}
		}
		
		boolean isMember(ClientThread c)
		{
			for(int i = 0; i < roomMembers.size(); i++)
			{
				ClientThread ct = roomMembers.get(i);
				if(c.equals(ct))
				{
					return true;
				}
			}	
			return false;
		}
		
		String getRoomName()
		{
			return this.name;
		}
		
		String memberList()
		{
			String list = new String();
			for(int i = 0 ; i < roomMembers.size(); i++)
			{
				list += " " + roomMembers.get(i).username;
			}

			return "Members online :" + list;
		}
		
		int getRoomNumber()
		{
			//return this.number;
			return 0;
		}
		
		void addUser(ClientThread c)
		{
			if(!roomMembers.add(c))
				display("error adding client");
		}
		
		void removeUser(ClientThread c)
		{
			roomMembers.remove(c);
		}
		
	}

	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String[] userpass; String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		String date;
		
		int room = 0;

		// Constructor
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			/* Creating both Data Stream */
			System.out.println("Thread trying to create Object Input/Output Streams");
			
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				String temp = (String) sInput.readObject();
				userpass = temp.split(":");
				String user = userpass[0]; 
				String pass = userpass[1];
				
				String line = "";
				boolean flag = false;
				
				try
				{
					Connection conn = 
					DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/chatDB?user=root&password=admin");
				
					Statement stmt = conn.createStatement();
					String checkLogin = "SELECT password FROM users WHERE username='" + user + "';";
					ResultSet rs = stmt.executeQuery(checkLogin);
					rs.next();
					String passDB = rs.getString("password");
					
					if(passDB.equals(pass)) 
					{
						display(user + " just connected.");
						username = user;
						flag = true;
					}
				
					if (flag == false) 
					{ 
						String tmp = "denied";
						sOutput.writeObject(tmp);
					}
				}
				catch (SQLException e)
				{
					System.out.println("SQLException: " + e.getMessage());
					System.out.println("SQLState: " + e.getSQLState());
					System.out.println("VendorError: " + e.getErrorCode());
				}
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
			
            date = new Date().toString() + "\n";
		}

		// what will run forever
		public void run() {
			// to loop until LOGOUT
			boolean online = true;
			while(online) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message = cm.getMessage(username);

				// Switch on the type of message receive
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					//broadcast(username + ": " + message);
					broadcastToRooms(username + ": " + message , this);
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected.");
					online = false;
					for(int i = 0; i < rooms.size(); i++)
					{
						if(rooms.get(i).isMember(this))
						{
							rooms.get(i).removeUser(this);
							rooms.get(i).broadcastRooms(username + " has left the room");
						}
					}
					break;
				case ChatMessage.WHOISIN:
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					// scan al the users connected
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					writeMsg("List of rooms currently open : ");
					for(int i = 0; i < rooms.size(); i++)
					{
						ChatRoom tempRoom = new ChatRoom(rooms.get(i));
						writeMsg(tempRoom.getRoomName() + " " + tempRoom.memberList());
					}
					break;
				case ChatMessage.CREATEROOM:
					//broadcast(username + "has Joined room  : " + message);			
					ChatRoom tempRoom = new ChatRoom(message);	
					tempRoom.addUser(this);
					tempRoom.broadcastRooms(username + " has joined the room " + tempRoom.getRoomName());
					rooms.add(tempRoom);
					numberOfRooms++;
					break;
				case ChatMessage.JOINROOM:
					for(int i = 0 ; i < rooms.size(); i++)
					{
						if(rooms.get(i).getRoomName().equalsIgnoreCase(message))
						{
							rooms.get(i).addUser(this);
							rooms.get(i).broadcastRooms(username + " has joined the room "  
								+ rooms.get(i).getRoomName());
						}
					}
					break;
				case ChatMessage.LEAVE:
					for(int i = 0; i < rooms.size(); i++)
					{
						if(rooms.get(i).isMember(this))
						{
							rooms.get(i).removeUser(this);
							rooms.get(i).broadcastRooms(username + " has left the room");
						}
					}
					break;
				}
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}
		
		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}
