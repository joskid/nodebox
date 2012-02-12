package nodebox.function;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class StringFunctionsTest {

    @Test
    public void testLength() {
        assertEquals(0, StringFunctions.length(null));
        assertEquals(0, StringFunctions.length(""));
        assertEquals(5, StringFunctions.length("bingo"));
    }

    @Test
    public void testWordCount() {
        assertEquals(0, StringFunctions.wordCount(null));
        assertEquals(0, StringFunctions.wordCount(""));

        assertEquals(1, StringFunctions.wordCount("a"));
        assertEquals(1, StringFunctions.wordCount("a_b"));

        assertEquals(2, StringFunctions.wordCount("a b"));
        assertEquals(2, StringFunctions.wordCount("a-b"));
        assertEquals(2, StringFunctions.wordCount("a,b"));
        assertEquals(2, StringFunctions.wordCount("a.b"));
    }

}
