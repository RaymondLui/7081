import java.io.*;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server. 
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no 
 * need to count bytes or to wait for a line feed at the end of the frame
 */
public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// The different types of message sent by the Client
	// WHOISIN to receive the list of the users connected
	// MESSAGE an ordinary message
	// LOGOUT to disconnect from the Server
	static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2, CREATEROOM = 3, JOINROOM = 4, LEAVE = 5;
	private int type;
	private String message;
	private String [] userInput;
	
	// constructor
	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	// getters
	int getType() {
		return type;
	}
	
	
	
	String getMessage(String username) {
		
		
		if(message.startsWith("--leave"))
		{
			type = 5;
			return "--leave";
		}
		else
		if(message.startsWith("--join"))
		{
			userInput = message.split(" ");
			if(userInput.length == 2 )
			{
				type = 4;
				return userInput[1];
			}
		}
		else
		if(message.startsWith("--room"))
		{
			userInput = message.split(" ");
			if(userInput.length == 2)
			{
				type = 3;
				return userInput[1];
			}
		}
		else
		if (message.startsWith("--adduser")) {
			// usage: --adduser <name> <access#>
			String line = "";
			try{						
				BufferedReader br = new BufferedReader(new FileReader("pass.txt"));
				while((line = br.readLine()) != null) {
					String [] dbAccess = line.split(":");
					if(dbAccess[0].equals(username) && dbAccess[2].equals("777"))
					{
						userInput = message.split(" ");
						if(userInput.length != 4)
						{
							return("Usage: --adduser <username> <password> <access>");
						}
						else 
						{
							String newUser = userInput[1];
							String newPass = userInput[2];
							String accessCode = userInput[3];
							BufferedWriter bw = new BufferedWriter(new FileWriter("pass.txt", true));
							String newID = newUser + ":" + newPass + ":" + accessCode;
							bw.append(newID);
							bw.newLine();
							bw.close();
							return(newUser + " has been created.");
						}
					}
					if(dbAccess[0].equals(username) && dbAccess[2].equals("666"))
					{
						String [] userInput = message.split(" ");
						if(userInput.length != 4 || !(userInput[3].equals("555")))
						{
							return("Usage: --adduser <username> <password> <access>" + 
								"Scrum Master may only add/remove developers");
						}
						else
						{
							String newUser = userInput[1];
							String newPass = userInput[2];
							String accessCode = userInput[3];
							BufferedWriter bw = new BufferedWriter(new FileWriter("pass.txt", true));
							String newID = newUser + ":" + newPass + ":" + accessCode;
							bw.append(newID);
							bw.newLine();
							bw.close();
							return(newUser + " Developer has been created.");
						}
					}
				}
			} catch (IOException e) {
				return("Errors: " + e);
			}
		}
		return message;
	}
}

