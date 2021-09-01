package testing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;


@TestMethodOrder(OrderAnnotation.class)
class TestProtocol {

	static Socket testSocket;
	static DataOutputStream outToServer;
	static BufferedReader inFromServer;
	static String res;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		
		testSocket = new Socket("localhost", 6789);
		 
		outToServer = 
	    new DataOutputStream(testSocket.getOutputStream());
		
		inFromServer = new BufferedReader(new
        InputStreamReader(testSocket.getInputStream()));
				 
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {		

		outToServer.writeBytes("done\n");
		res = inFromServer.readLine();
		
		testSocket.close(); 
	}

	
	@Test
	@Order(1)   
	void test_user_login_and_CDIR_command() throws Exception{
		
		res = inFromServer.readLine();
		assertTrue(res.contains("+"));
		
		outToServer.writeBytes("USER user3\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("+"));	
		
		outToServer.writeBytes("ACCT acct3\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("+"));		
		
		outToServer.writeBytes("PASS pass2\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("-"));
		
		outToServer.writeBytes("PASS pass3\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("!"));		
		
		outToServer.writeBytes("CDIR src\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("!"));	
		
		outToServer.writeBytes("LIST f\n");
		String temp = "";
		res= "";
		while(!temp.equals("\0")) {
			temp = inFromServer.readLine();
			res  += temp + "\n";
		}
		assertTrue(res.contains("+"));	
//		System.out.print(res);

		// Changing back to reset directory for further tests
		outToServer.writeBytes("CDIR ..\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("!"));	

	}
		


	@Test
	@Order(2)   
	void test_unauthenticated_commands() throws Exception {
	
		outToServer.writeBytes("USER user4\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("+"));	
		
		outToServer.writeBytes("LIST F src\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("-"));		
		
		outToServer.writeBytes("NAME src\\renameFile\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("-"));
		
		outToServer.writeBytes("DONE\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("-"));
	}

	
	@Test
	@Order(3)   
	void testCDIR_before_login() throws Exception{
		
		outToServer.writeBytes("USER user4\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("+"));	
		
		outToServer.writeBytes("CDIR src\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("-"));		
		
		outToServer.writeBytes("PASS pass4\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("!"));
		
		outToServer.writeBytes("CDIR src\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("!"));	
				
		outToServer.writeBytes("LIST V\n");
		String temp = "";
		res= "";
		while(!temp.equals("\0")) {
			temp = inFromServer.readLine();
			res  += temp + "\n";
		}
		assertTrue(res.contains("+"));	
//		System.out.print(res);

		// Changing back to reset directory for further tests
		outToServer.writeBytes("CDIR ..\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("!"));	
	}

	
	@Test
	@Order(4)   
	void test_NAME_and_KILL_commands() throws Exception {
		
		outToServer.writeBytes("USER user1\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("!"));	

//		outToServer.writeBytes("CDIR src\\testFiles\n");
//		res = inFromServer.readLine();
//		assertTrue(res.contains("!"));	
//		
//		outToServer.writeBytes("NAME renameFile\n");
//		res = inFromServer.readLine();
//		assertTrue(res.contains("+"));
//		
//		outToServer.writeBytes("TOBE deleteFile\n");
//		res = inFromServer.readLine();
//		assertTrue(res.contains("+"));
//
//		outToServer.writeBytes("KILL renameFile\n");
//		res = inFromServer.readLine();
//		assertTrue(res.contains("-"));	
//		
//		outToServer.writeBytes("CDIR ..\n");
//		res = inFromServer.readLine();
//		assertTrue(res.contains("!"));	
//		
//		outToServer.writeBytes("KILL testFiles\\deleteFile\n");
//		res = inFromServer.readLine();
//		assertTrue(res.contains("+"));	
//		
		// Changing back to reset directory for further tests
//		outToServer.writeBytes("CDIR ..\n");
//		res = inFromServer.readLine();
//		assertTrue(res.contains("!"));	
		
	}
	
	
	@Test
	@Order(5)   
	void test_invalid_commands() throws Exception {
		
		outToServer.writeBytes("USER user4\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("+"));
		
		outToServer.writeBytes("PASS pass4\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("!"));
		
		outToServer.writeBytes("TOBE src\\testFiles\\deleteFile\n");
		res = inFromServer.readLine();
		assertTrue(res.contains("-"));
		
	}
	
}
