package client;
/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*;


class Client { 


	private static int receiveFileSize = 0;
	private static String recieveFileName = "";
	
    public static void main(String argv[]) throws Exception 
    { 
        String cmd; 
        String response = ""; 
        boolean done = false;
        
        BufferedReader inFromUser = 
	    new BufferedReader(new InputStreamReader(System.in)); 
	
        Socket clientSocket = new Socket("localhost", 6789); 
	
        DataOutputStream outToServer = 
	    new DataOutputStream(clientSocket.getOutputStream()); 

        BufferedReader inFromServer = 
            new BufferedReader(new
            InputStreamReader(clientSocket.getInputStream()));
        
        // Wait for the initial greeting
        String res = inFromServer.readLine(); 
        System.out.print("FROM SERVER: " + res + "\n"); 
        
        while (!done) { 
            
            cmd = inFromUser.readLine(); 
        
            outToServer.writeBytes(cmd + '\n'); 

            res = inFromServer.readLine();  
            
            if (cmd.toUpperCase().contains("LIST") && res.startsWith("+")) {
            	while (!res.equals("\0")) {
            		response += res + "\n";
            		res = inFromServer.readLine();
            	}
            } else if (cmd.equalsIgnoreCase("SEND")) {
            	
            	byte[] bytes = new byte[receiveFileSize];
            	clientSocket.setSoTimeout(6000);
            	
            	try {
        			for (int i = 0; i < receiveFileSize; i++) {
        				bytes[i] = (byte) clientSocket.getInputStream().read();
        			}
        			
        			// Write file
    				FileOutputStream createdFile = new FileOutputStream(System.getProperty("user.dir") + "/clientFiles/" + recieveFileName);
    				createdFile.write(bytes);
    				createdFile.close();	  
    				String r = inFromServer.readLine();
    				System.out.println(r);
        			
            	} catch (SocketTimeoutException e) {
	    			// Stop socket timeout immediately
	    			clientSocket.setSoTimeout(0);
	    			System.out.println("Could not receive file");
	    		}catch(Exception e) {
	    			clientSocket.setSoTimeout(0);
	    		}

            	
            	
            } else {            
            	response = res + "\n";
            }
            
            if (cmd.toUpperCase().contains("RETR")) {
            	receiveFileSize = Integer.valueOf(response);
            	recieveFileName = cmd.substring(5);
            }
            
            if (cmd.toUpperCase().equals("DONE") && response.startsWith("+")) {
                done = true;                
            } else {
            	done = false;
            }
   
            System.out.print("FROM SERVER: " + response); 
            response = "";
            
        }

        clientSocket.close(); 
	
    } 
} 
