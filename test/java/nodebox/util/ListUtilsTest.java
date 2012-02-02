package nodebox.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;

import static junit.framework.Assert.assertSame;
import static nodebox.util.ListUtils.listClass;

public class ListUtilsTest {
    
    @Test
    public void testListClass() {
        assertSame(Integer.class, listClass(Lists.newArrayList(1, 2)));
        assertSame(String.class, listClass(ImmutableList.of("a", "b")));
        assertSame(Object.class, listClass(Lists.newArrayList(1, 2.0)));
        assertSame(Object.class, listClass(ImmutableList.of()));
        assertSame(Object.class, listClass(Lists.newArrayList(1, null, 2)));
        assertSame(Object.class, listClass(Lists.newArrayList(null, null, 1)));
    }

}
