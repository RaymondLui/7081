import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Raymond
 */
public class ChatMessageIT {

    private ChatMessage chatmessage;
    private int type;
    private String message;
    
    @Before
    public void setUp() {
        type = 5;
        message = "--leave"; 
        chatmessage = new ChatMessage(type, message);
    }
    
    @After
    public void tearDown() {
        type = 0;
        message = "";
    }
    
    /**
     * Test of getType method, of class ChatMessage.
     * Ensure it returns what "type" is currently set to.
     */
    @Test
    public void testGetType() {
        int expResult = 5;
        assertEquals(expResult, chatmessage.getType());
    }

    /**
     * Test of getMessage method, of class ChatMessage.
     */
    @Test
    public void testGetMessage() {
        String expResult = "--leave";
        assertEquals(expResult, chatmessage.getMessage("Admin"));
    }
    
}
