package nodebox.node;

public class TestNodes extends NodeLibrary {

    public static final String LIBRARY_NAME = "testlib";

    public TestNodes() {
        super(LIBRARY_NAME);
    }

    public static class IntVariable extends Node {

        final Port valueIn, resultOut;

        public IntVariable(Macro parent) {
            super(parent);
            setExported(true);
            valueIn = createPort("value", Integer.class, Port.Direction.IN);
            resultOut = createPort("result", Integer.class, Port.Direction.OUT);
        }

        @Override
        public void cook(CookContext context) {
            resultOut.setValue(process(valueIn.asInt()));
        }

        public int process(int v) {
            return v;
        }
    }

    public static class Negate extends IntVariable {

        public Negate(Macro parent) {
            super(parent);
        }

        @Override
        public int process(int v) {
            return -v;
        }
    }

    public static class Binary extends Node {

        final Port v1In, v2In, resultOut;

        public Binary(Macro parent) {
            super(parent);
            setExported(true);
            v1In = createPort("v1", Integer.class, Port.Direction.IN);
            v2In = createPort("v2", Integer.class, Port.Direction.IN);
            resultOut = createPort("result", Integer.class, Port.Direction.OUT);
        }

        @Override
        public void cook(CookContext context) {
            resultOut.setValue(process(v1In.asInt(), v2In.asInt()));
        }

        public int process(int v1, int v2) {
            return v1;
        }

    }

    public static class Add extends Binary {

        public Add(Macro parent) {
            super(parent);
        }

        @Override
        public int process(int v1, int v2) {
            return v1 + v2;
        }

    }

    public static class Multiply extends Binary {

        public Multiply(Macro parent) {
            super(parent);
        }

        @Override
        public int process(int v1, int v2) {
            return v1 * v2;
        }

    }

    public static class MultiAdd extends Node {

        public MultiAdd(Macro parent) {
            super(parent);
            setExported(true);
        }

        @Override
        public void cook(CookContext context) throws RuntimeException {
            throw new UnsupportedOperationException("This class is not yet implemented. Waiting for multiports.");
        }

    }

    public static class FloatNegate extends Node {

        final Port valueIn, resultOut;

        public FloatNegate(Macro parent) {
            super(parent);
            setExported(true);
            valueIn = createPort("v1", Float.class, Port.Direction.IN);
            resultOut = createPort("result", Float.class, Port.Direction.OUT);
        }

        public void cook(CookContext context) throws RuntimeException {
            float value = valueIn.asFloat();
            resultOut.setValue(-value);
        }
    }

    public static class ConvertToUppercase extends Node {

        final Port valueIn, resultOut;

        public ConvertToUppercase(Macro parent) {
            super(parent);
            setExported(true);
            valueIn = createPort("value", String.class, Port.Direction.IN);
            resultOut = createPort("result", String.class, Port.Direction.OUT);
        }

        @Override
        public void cook(CookContext context) throws RuntimeException {
            String value = valueIn.asString();
            resultOut.setValue(value.toUpperCase());
        }

    }

    public static class Crash extends Node {

        public Crash(Macro parent) {
            super(parent);
            setExported(true);
            createPort("value", Integer.class, Port.Direction.IN);
            createPort("result", Integer.class, Port.Direction.OUT);
        }

        @Override
        public void cook(CookContext context) throws RuntimeException {
            int a = 0;
            System.out.println(1 / a);
        }

    }

    public static class TestNetwork extends Macro {

        public TestNetwork(Macro parent) {
            super(parent);
            setExported(true);
        }

    }

}
