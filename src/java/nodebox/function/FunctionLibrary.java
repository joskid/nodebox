package nodebox.function;

import com.google.common.base.Objects;
import nodebox.util.LoadException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * A collection of functions. This collection is contained in a namespace.
 */
public abstract class FunctionLibrary {

    private static final Pattern HREF_PATTERN = Pattern.compile("^([a-z]+):(.*)$");

    public static FunctionLibrary load(String href) {
        Matcher hrefMatcher = HREF_PATTERN.matcher(href);
        checkArgument(hrefMatcher.matches(), "Library identifier should be in the form language:filename.ext");
        checkState(hrefMatcher.groupCount() == 2);
        String language = hrefMatcher.group(1);
        String identifier = hrefMatcher.group(2);
        if (language.equals("java")) {
            return JavaLibrary.loadStaticClass(identifier);
        } else if (language.equals("clojure")) {
            return ClojureLibrary.loadScript(identifier);
        } else if (language.equals("python")) {
            return PythonLibrary.loadScript(identifier);
        } else {
            throw new LoadException(language, "Unknown function library type " + language + ".");
        }
    }

    public static FunctionLibrary ofClass(String namespace, Class c, String... methodNames) {
        return JavaLibrary.ofClass(namespace, c, methodNames);
    }

    public abstract String getNamespace();

    /**
     * Get the name of the library as it should be written when linking to this library.
     * <p/>
     * The format is "language:filename.extension" ie. "clojure:voronoi.clj".
     *
     * @return A link to a library.
     */
    public abstract String getLink();

    public abstract Function getFunction(String name);

    public abstract boolean hasFunction(String name);


    //// Object overrides ////

    @Override
    public int hashCode() {
        return Objects.hashCode(getNamespace());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FunctionLibrary)) return false;
        final FunctionLibrary other = (FunctionLibrary) o;
        return Objects.equal(getNamespace(), other.getNamespace());
    }

    @Override
    public String toString() {
        return String.format("<FunctionLibrary %s>", getNamespace());
    }

}
