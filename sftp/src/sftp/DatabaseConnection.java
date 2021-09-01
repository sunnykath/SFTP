package sftp;

import java.sql.*;

public class DatabaseConnection { 


    public static Connection connect()
    { 
    	String currentDir = System.getProperty("user.dir");
        String jdbcURL = "jdbc:sqlite:" + currentDir +"/src/sftp/userInfo.db";
        Connection  conn = null;
	    
        try { 
	        conn = DriverManager.getConnection(jdbcURL);
		
	        System.out.println("Connection to SQLite has been established.");
	        
        
    	} catch (SQLException e) {
    		System.out.println(e.getMessage());
    	}

        return conn;

    } 
    
    
    
} 
