package nodebox.function;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nodebox.graphics.Point;
import nodebox.node.Port;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class Functions {

    private static final ImmutableMap<String, String> ARGUMENT_TYPE_MAPPING;

    static {
        ARGUMENT_TYPE_MAPPING = ImmutableMap.of(
                "int", Port.TYPE_INT,
                "float", Port.TYPE_FLOAT,
                String.class.getName(), Port.TYPE_STRING,
                Point.class.getName(), Port.TYPE_POINT
        );
    }

    public static ImmutableList<Function.Argument> introspect(Method method) {
        UniquePrefixMap prefixMap = new UniquePrefixMap();
        // Find all parameters for this method.
        ImmutableList.Builder<Function.Argument> builder = ImmutableList.builder();
        for (Class parameter : method.getParameterTypes()) {
            String type = Objects.firstNonNull(ARGUMENT_TYPE_MAPPING.get(parameter.getName()), parameter.getName());
            String name = prefixMap.nextWithPrefix(type);
            builder.add(new Function.Argument(name, type));
        }
        return builder.build();
    }

    public static Method findMethod(Class c, String methodName) {
        Method[] methods = c.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Class " + c + " does not have a method " + methodName);
    }


    private static class UniquePrefixMap {

        final Map<String, Integer> prefixMap = new HashMap<String, Integer>();

        public synchronized String nextWithPrefix(String prefix) {
            int number = Objects.firstNonNull(prefixMap.get(prefix), 0);
            number++;
            prefixMap.put(prefix, number);
            return prefix + number;
        }
    }

}
