package test.java;

import main.APIServer;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class testIsSubsequence {
    @Test
    public void testIsSub(){
        assertTrue(APIServer.isSubsequence(new String[]{"a"}, new String[]{"a"}));
        assertTrue(APIServer.isSubsequence(new String[]{"a", "b"}, new String[]{"a"}));
        assertFalse(APIServer.isSubsequence(new String[]{"a", "b"}, new String[]{"a", "b", "c"}));
        assertTrue(APIServer.isSubsequence(new String[]{"a", "b", "c", "d"}, new String[]{"a", "b", "d"}));
        assertFalse(APIServer.isSubsequence(new String[]{"a", "b", "c", "d"}, new String[]{"a", "d", "c"}));
    }
}
