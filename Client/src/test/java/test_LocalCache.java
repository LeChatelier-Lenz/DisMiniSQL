import config.TableLocationCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class test_LocalCache {
    private TableLocationCache cache;

    @BeforeEach
    public void setUp() {
        cache = new TableLocationCache();
    }

    @Test
    public void testCacheAndGetIP() {
        cache.cache("users", "192.168.1.1");
        assertEquals("192.168.1.1", cache.getIP("users"));
    }

    @Test
    public void testCaseInsensitiveCache() {
        cache.cache("Orders", "10.0.0.2");
        assertEquals("10.0.0.2", cache.getIP("orders"));
        assertTrue(cache.contains("ORDERS"));
    }

    @Test
    public void testRemove() {
        cache.cache("products", "127.0.0.1");
        assertTrue(cache.contains("products"));
        cache.remove("Products");
        assertFalse(cache.contains("products"));
    }

    @Test
    public void testNonexistentEntry() {
        assertNull(cache.getIP("nonexistent"));
        assertFalse(cache.contains("nonexistent"));
    }
}
