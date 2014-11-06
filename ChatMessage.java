import java.io.*;
import java.sql.*;

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
	
	String getMessage(String username) 
	{
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
			
			// New User Variables.
			String newUser = "";
			String newPass = "";
			String accessGroup = "";
			
			// The userType of the command issuer.
			String userType = "";
			
			try
			{
				Connection conn = 
				DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/chatDB?user=root&password=admin");
				
				Statement one = conn.createStatement();
				Statement two = conn.createStatement();
				Statement three = conn.createStatement();
				Statement four = conn.createStatement();
				
				// Check if an administrator issued the command.
				String checkForAdmin = "SELECT usertype FROM users WHERE username='" + username + "';";
				ResultSet rs = one.executeQuery(checkForAdmin);
				rs.next();
				userType = rs.getString("usertype");
				
				userInput = message.split(" ");
				
				if(userInput.length != 4)
				{
					return("Usage: --adduser <username> <password> <access>");
				}
				else
				{
					newUser = userInput[1];
					newPass = userInput[2];
					accessGroup = userInput[3];
				}
				
				// Check if the username already exists in DB.
				String checkUsername = "SELECT COUNT(1) FROM users WHERE username='" 
					+ newUser + "';";
				ResultSet r = two.executeQuery(checkUsername);
				r.next();
						
				// 0 = Username does not exist in DB, 1 = Exists
				long userExists = r.getLong("COUNT(1)");
				if (userExists == 0 && userType.equals("admin")) 
				{
					String createUser = "INSERT INTO users VALUES (\"" + 
						newUser + "\",\"" + newPass + "\",\"" + accessGroup + "\");";
					three.executeUpdate(createUser);
					return(newUser + " has been created.");
				} 
				else if(userExists == 0 && userType.equals("master"))
				{
					if(!accessGroup.equals("dev"))
					{
						return("Scrum Master may only add/remove developers");
					}
					else
					{
						String createUser = "INSERT INTO users VALUES (\"" + 
							newUser + "\",\"" + newPass + "\",\"" + accessGroup + "\");";
						four.executeUpdate(createUser);
						return(newUser + " Developer has been created.");
					}
				}
				else if(!(userType.equals("admin") || userType.equals("master")))
				{
					return("You do not have permission to add users.");
				}
				else
				{
					return("Username already exists.");
				}
			}
			catch (SQLException e)
			{
				System.out.println("SQLException: " + e.getMessage());
				System.out.println("SQLState: " + e.getSQLState());
				System.out.println("VendorError: " + e.getErrorCode());
			}
		}
		return message;
	}
}

