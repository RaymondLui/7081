/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Raymond
 */
public class ClientIT {
    
    private Client client;
    private String server;
    private int port;
    private String username;
    private String password;
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        server = "localhost";
        port = 1500;
        username = "Admin";
        password = "password";
        client = new Client(server, port, username, password);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of start method, of class Client.
     * Expecting "false" (assuming no server is running).
     */
    @Test
    public void testStart() {
        System.out.println("start");
        boolean expResult = false;
        boolean result = client.start();
        assertEquals(expResult, result);
    }
}
