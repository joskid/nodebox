package nodebox.util;

import static junit.framework.Assert.assertEquals;

public class InfiniteListTest {
    
    public void testBasic() {
        final String hello = "HELLO";
        InfiniteList<String> list = InfiniteList.of("hello");
        assertEquals(list.get(0), hello);
        assertEquals(list.get(2), hello);
        assertEquals(list.get(8888), hello);
    }

}
