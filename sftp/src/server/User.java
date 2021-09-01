package server;


import java.sql.*;

import sftp.DatabaseConnection;

public class User {
	
	static private String user_id = "";
	static private String acct = "";
	static private String pass = "";

	static private boolean acctNeeded;
	static private boolean passNeeded;
	static private boolean loggedIn = false;
 
	
	static Connection conn = DatabaseConnection.connect();
	

	public static boolean validUser_id(String user) throws SQLException { 
		
		Statement statement = conn.createStatement();
		
		String query = String.format("SELECT * FROM users WHERE user = \"%s\"", user);
		
		ResultSet result = statement.executeQuery(query);
		
		try {
			user_id = result.getString("User");
			

			acctNeeded = !(result.getString("acct").contentEquals(""));
			passNeeded = !(result.getString("pass").contentEquals(""));
			loggedIn = !(acctNeeded || passNeeded);
			
			
			return true;
		} 
		catch (SQLException e) {
			return false;
		}
		
	}

	public static boolean validAcct (String account) throws SQLException { 
		
		Statement statement = conn.createStatement();
		
		String query = String.format("SELECT * FROM users WHERE acct = \"%s\"", account);
		
		ResultSet result = statement.executeQuery(query);
		
		try {
			String userDB = result.getString("user");
			
			if (userDB.equals(user_id)) {
				acct = account;
			} else {
				return false;
			}
			
//			System.out.println(String.format("Acct is set to -> %s", acct));
			
			acctNeeded = false;
			passNeeded = !(result.getString("pass").equals(pass));
			
			loggedIn = !(passNeeded);
			
			return true;
		} 
		catch (SQLException e) {
			return false;
		}
		
	}
	
	public static boolean validPass (String password) throws SQLException { 
		
		Statement statement = conn.createStatement();
		
		String query = String.format("SELECT * FROM users WHERE user = \"%s\"", user_id);
		
		ResultSet result = statement.executeQuery(query);
		
		try {
			String passDB = result.getString("pass");
			
			if (passDB.equals(password)) {
				pass = password;
			} else {
				return false;
			}
			
//			System.out.println(String.format("Pass is set to -> %s", pass));
			
			acctNeeded = !((result.getString("acct").equals(acct)) || (result.getString("acct").equals("")));
			passNeeded = false;
			
			loggedIn = !(acctNeeded);
			
			return true;
		} 
		catch (SQLException e) {
			return false;
		}
		
	}
	
	
	public static boolean isAcctNeeded() {
		return acctNeeded;
	}


	public static boolean isPassNeeded() {
		return passNeeded;
	}


	public static boolean isLoggedIn() {
		return loggedIn;
	}
	

}
