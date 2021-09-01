package server;
/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;


class Server { 
    
	static private enum Type {
		Ascii,
		Binary,
		Continuous
	}
	
	static private Type fileType = Type.Binary; 
	
	static private String projectDir = System.getProperty("user.dir");
	static private String currentDir = projectDir;
	static private boolean fileChange = false;
	static private String fileOldName = "";
	static boolean sendFile= false;
	static File fileToSend;
	
    public static void main(String argv[]) throws Exception 
    { 
		String clientSentence = ""; 
		String response = ""; 
		boolean done = false;
		
		ServerSocket welcomeSocket = new ServerSocket(6789); 
		Socket connectionSocket = welcomeSocket.accept(); 
		
		BufferedReader inFromClient = 
		new BufferedReader(new
			InputStreamReader(connectionSocket.getInputStream())); 
		
		OutputStream oS = connectionSocket.getOutputStream();
		DataOutputStream  outToClient = 
		new DataOutputStream(oS);


		outToClient.writeBytes("+SKAT-736 SFTP Service\n");
		

		while(!done) { 

			clientSentence = inFromClient.readLine(); 

			String[] cmds = clientSentence.split(" ", 2);
			String cmd = "";
			String args = "";
			
			try {
				cmd = cmds[0];
				args = cmds[1];
//				System.out.println(args);
			} catch (Exception e) {
				//TODO: handle exception
//				System.out.println(cmd);
//				System.out.println(e.getMessage());
			}

			if (cmd.equalsIgnoreCase("USER")){
				response = handleUser(args);
			} else if (cmd.equalsIgnoreCase("ACCT")){
				response = handleAcct(args);
			} else if (cmd.equalsIgnoreCase("PASS")){
				response = handlePass(args);
			} else if (User.isLoggedIn()) { 
				if (cmd.equalsIgnoreCase("TYPE")){
					response = handleType(args);
				} else if (cmd.equalsIgnoreCase("CDIR")){
					response = handleCdir(args);
				} else if (cmd.equalsIgnoreCase("LIST")){
					response = handleList(args);			
				} else if (cmd.equalsIgnoreCase("KILL")){
					response = handleKill(args);			
				} else if (cmd.equalsIgnoreCase("NAME") || cmd.equalsIgnoreCase("TOBE")){
					response = handleRename(cmd, args);					
				} else if (cmd.equalsIgnoreCase("RETR") || cmd.equalsIgnoreCase("SEND") || cmd.equalsIgnoreCase("STOP")){
					response = handleRetr(cmd, args, oS);					
				} else if (cmd.equalsIgnoreCase("DONE")){
					response = "+SKAT-736 closing connection\n";
					done = true;					
				} else {			
					response = "-Invalid command, try again\n";
				}
			} else {			
				response = "-Please log-in first\n";
			}

			outToClient.writeBytes(response);
			outToClient.flush();
		
		}
	
		welcomeSocket.close(); 
	}
    
    
    private static String handleUser(String user_id) throws SQLException {
    	
		boolean status = false;
		String res = "";
		
		status = User.validUser_id(user_id);
		
		if (status) {
			// Valid user
			if (User.isLoggedIn()) {
				// Send in Logged in Response
				res = "!" + user_id + " logged in\n";
			} else if (User.isAcctNeeded() && User.isPassNeeded()) {
				// Ask for Account and Password
				res = "+User-id valid, send account and password\n";
			} else if (User.isAcctNeeded()) {
				// Ask for Account only
				res = "+User-id valid, send account\n";
			} else {
				// Ask for Password Only
				res = "+User-id valid, send password\n";
			}
			
		} else {
			// Invalid User
			res = "-Invalid user-id, try again\n";
		}
		
		return res;	
    	
    }
    
    
    private static String handleAcct(String acct) throws SQLException {
    	
    	boolean status = false;
		String res = "";
		
		status = User.validAcct(acct);
		
		if (status) {
			// Valid Account
			if (User.isLoggedIn()) {
				// Send in Logged in Response
				res = "!Account valid, logged in\n";
			} else {
				// Ask for Password
				res = "+Account valid, send password\n";
			}
			
		} else {
			// Invalid Account
			res = "-Invalid account, try again\n";
		}
		
		return res;	
    	
    }
    

    private static String handlePass(String pass) throws SQLException {
    	
    	boolean status = false;
		String res = "";
		
		status = User.validPass(pass);
		
		if (status) {
			// Valid Password
			if (User.isLoggedIn()) {
				// Send in Logged in Response
				res = "!Logged in\n";
			} else {
				// Ask for Account
				res = "+Send Account\n";
			}
			
		} else {
			// Invalid Password
			res = "-Wrong password, try again\n";
		}
		
		return res;	
    	
    }
    
    
    private static String handleType(String type) {
    	
    	String res = "";
    	boolean status = true;
    	
    	switch (type) {
		    case "A": 
				fileType = Type.Ascii;
				break;
		    case "B": 
				fileType = Type.Binary;
				break;
		    case "C": 
				fileType = Type.Continuous;
				break;
		    default:
		    	status = false;
		    	fileType = Type.Binary;
    	}
    	
    	if (status) {
    		// Type Changed
    		res = "+Using " + fileType.name() + " mode\n";
    	} else {
    		res = "-Type not valid\n";
    	}
    	
    	return res;
    }
    
    
    private static String handleList(String args) {
    	
    	String res = "";
    	String format = "F";
    	String dir =  currentDir;

    	    	
    	if (args.length() > 2) {
    		String[] str = args.split(" ", 2);
    		format = str[0];
    		if (format.length() > 1) {
    			res = "-Invalid command, try again\n";
    			return res;
    		}
    		dir = str[1];
    	} else {
    		format = args;
    	}

    	File folder = new File(dir);
    	String size, lastMod, hidden;
    	
    	if (folder.isDirectory()) {
	    	File[] files = folder.listFiles();
	    
	    	res = "+" + dir + "\r\n";

    		if (format.equalsIgnoreCase("V")) {
    			res += "Name\t\t\tType\tSize\tLast Modified\tHidden\r\n";
    		}
	    	    	
	    	for (int i = 0; i < files.length; i++) {
	    		File f = files[i];
				res += f.getName();
	    		if (format.equalsIgnoreCase("V")) {
	    			// Verbose Info
	    			size = String.valueOf(f.length());
	    			lastMod = String.valueOf(new Date(f.lastModified()));
	    			hidden = String.valueOf(f.isHidden());
	    			res += "\t\t\t";
	    			if (f.isDirectory()) {
	    				res += "Folder"; 
	    			} else {
	    				res += "File";
	    			}
					res += "\t" + size + "\t" + lastMod + "\t" + hidden ;
	    		}
	    		res += "\r\n";
	    	}
	    	
	    	res = res + "\0\n";
    	} else {
    		res = "-Directory doesn't exist try again\n";
    	}
    	
    	return res;
    }
    
    
    private static String handleKill(String arg) {
    	
    	String res = "";
    	
    	File fileDel = new File(currentDir + "\\" + arg);
    	
    	if (fileDel.exists()) {
    		try {
    			fileDel.delete();
    			res = "+" + fileDel.getAbsolutePath() + " deleted\n";
    		} catch (SecurityException e) {
    			res = "-Not deleted because file is protected\n";
    		}
    	} else {
    		res = "-Not deleted because such file doesn't exist\n";
    	}
    	
    	
    	return res;
    }
    
    
    private static String handleRename(String cmd, String arg) {
    	
    	String res = "";
    	
    	if (cmd.equalsIgnoreCase("NAME")) {

        	File fileToRename = new File(currentDir + "\\" + arg);
        	
        	if (fileToRename.exists()) {
        		// File does exist, send TOBE
        		fileChange = true;
        		fileOldName = arg;
        		res = "+File Exists, Send TOBE\n";
        	} else {
        		// File no exist
        		fileChange = false;
        		res = "-Can't find " + arg + "\n";
        	}

    	} else {
    		
    		if (fileChange) {
    			
    			File fileToRename = new File(currentDir + "\\" + fileOldName);
    			File newFile = new File(currentDir + "\\" + arg);
    			try {
    				fileToRename.renameTo(newFile);
    				fileChange = false;
    				res = "+" + fileOldName + " renamed to " + newFile.getAbsolutePath() + "\n";
    			} catch (SecurityException e) {
    				// File protected
    				res = "-File wasn't renamed because it's protected\n";
    			}
    			
    		} else {
    			// Send a successful NAME first
    			res = "-File wasn't renamed becuase a successful NAME command wasn't processed\n";
    		}
    		
    	}
    	
    	
    	
    	
    	return res;
    }
  
    
    private static String handleCdir(String arg) {
    	
    	String res = "";
    	
    	File cDir = new File(currentDir);
    	String nDir = "";
    	String tempDir = "";
    	
    	if (arg.contains("..")) {
    		// Move back a directory
    		nDir = cDir.getParent();
    		tempDir = nDir.concat(arg.substring(2));
    	} else {
    		tempDir = currentDir + "\\" + arg;
    	}
    	File dir = new File(tempDir);
    	
    	if (dir.exists()) {
    		currentDir = tempDir;
    		res = "!Changed working directory to " + dir.getAbsolutePath() + "\n";
    	} else {
			res = "-Can't connect to directory becuase directory doesn't exist\n";
    	}
	
    	return res;
    }
    
    
    private static String handleRetr(String cmd, String arg, OutputStream oS) {
    	
    	String res = "";
    	
    	
    	if (cmd.equalsIgnoreCase("RETR")) {
    		
    		fileToSend = new File(currentDir + "\\" + arg);
    		
    		if (fileToSend.exists()) {
    			String fileSize = String.valueOf(fileToSend.length());
    			res = fileSize + "\n";
        		sendFile = true;
    		} else {
    			res = "-File doesn't exist";
    			sendFile = false;
    		}
    		
    	} else if (!sendFile) {
    		res = "-Please send a RETR command first\n";
    		
    	} else if (cmd.equalsIgnoreCase("STOP")) {
    		
    		res = "+ok, RETR aborted\n";
    		sendFile = false;
    		
    	} else {
    		
    		byte[] content;
			try {
				content = Files.readAllBytes(fileToSend.toPath());
	    		
	    		oS.write(content);
	    		oS.flush();
				
	    		res = "+\n";
	    		
			} catch (IOException e) {
				res = "-\n";
			}
    		
    		sendFile = false;
    	}
    	    	
    	
    	return res;
    }
    
} 

