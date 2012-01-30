package nodebox.function;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class FunctionLibraryTest {

    @Test
    public void testLoadClojure() {
        String href = "clojure:test/clojure/math.clj";
        FunctionLibrary clojureLibrary = FunctionLibrary.load("clojure:test/clojure/math.clj");
        assertEquals("clojure-math", clojureLibrary.getNamespace());
        assertTrue(clojureLibrary.hasFunction("add"));
        assertEquals(href, clojureLibrary.getLink());
    }

    @Test
    public void testLoadPython() {
        String href = "python:test/python/functions.py";
        FunctionLibrary pythonLibrary = FunctionLibrary.load(href);
        assertEquals("functions", pythonLibrary.getNamespace());
        assertTrue(pythonLibrary.hasFunction("add"));
        assertEquals(href, pythonLibrary.getLink());
    }

    @Test
    public void testLoadJava() {
        String href = "java:nodebox.function.MathFunctions";
        FunctionLibrary javaLibrary = FunctionLibrary.load(href);
        assertEquals("math", javaLibrary.getNamespace());
        assertTrue(javaLibrary.hasFunction("add"));
        assertEquals(href, javaLibrary.getLink());
    }

}
