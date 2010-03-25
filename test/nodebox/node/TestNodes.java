package nodebox.node;

public class TestNodes extends NodeLibrary {

    public static final String LIBRARY_NAME = "testlib";

    public TestNodes() {
        super(LIBRARY_NAME);
        add(new IntVariable(this));
        add(new Negate(this));
        add(new Add(this));
        add(new Multiply(this));
        add(new MultiAdd(this));
        add(new FloatNegate(this));
        add(new ConvertToUppercase(this));
        add(new Crash(this));
        add(new TestNetwork(this));
    }

    public static class IntVariable extends Node {

        final Port valueIn, resultOut;

        public IntVariable(NodeLibrary library) {
            super(library);
            setExported(true);
            valueIn = addPort("value", Integer.class, Port.Direction.IN);
            resultOut = addPort("result", Integer.class, Port.Direction.OUT);
        }

        @Override
        public void cook(ProcessingContext context) {
            resultOut.setValue(process(valueIn.asInt()));
        }

        public int process(int v) {
            return v;
        }
    }

    public static class Negate extends IntVariable {

        public Negate(NodeLibrary library) {
            super(library);
        }

        @Override
        public int process(int v) {
            return -v;
        }
    }

    public static class Binary extends Node {

        final Port v1In, v2In, resultOut;

        public Binary(NodeLibrary library) {
            super(library);
            setExported(true);
            v1In = addPort("v1", Integer.class, Port.Direction.IN);
            v2In = addPort("v2", Integer.class, Port.Direction.IN);
            resultOut = addPort("v2", Integer.class, Port.Direction.OUT);
            addPort(v2In);
            addPort(resultOut);
        }

        @Override
        public void cook(ProcessingContext context) {
            resultOut.setValue(process(v1In.asInt(), v2In.asInt()));
        }

        public int process(int v1, int v2) {
            return v1;
        }

    }

    public static class Add extends Binary {

        public Add(NodeLibrary library) {
            super(library);
        }

        @Override
        public int process(int v1, int v2) {
            return v1 + v2;
        }

    }

    public static class Multiply extends Binary {

        public Multiply(NodeLibrary library) {
            super(library);
        }

        @Override
        public int process(int v1, int v2) {
            return v1 * v2;
        }

    }

    public static class MultiAdd extends Node {

        public MultiAdd(NodeLibrary library) {
            super(library);
            setExported(true);
        }

        @Override
        public void cook(ProcessingContext context) throws RuntimeException {
            throw new UnsupportedOperationException("This class is not yet implemented. Waiting for multiports.");
        }

    }

    public static class FloatNegate extends Node {

        final Port valueIn, resultOut;

        public FloatNegate(NodeLibrary library) {
            super(library);
            setExported(true);
            valueIn = addPort("v1", Float.class, Port.Direction.IN);
            resultOut = addPort("result", Float.class, Port.Direction.OUT);
        }

        public void cook(ProcessingContext context) throws RuntimeException {
            float value = valueIn.asFloat();
            resultOut.setValue(-value);
        }
    }

    public static class ConvertToUppercase extends Node {

        final Port valueIn, resultOut;

        public ConvertToUppercase(NodeLibrary library) {
            super(library);
            setExported(true);
            valueIn = addPort("value", String.class, Port.Direction.IN);
            resultOut = addPort("result", String.class, Port.Direction.OUT);
        }

        @Override
        public void cook(ProcessingContext context) throws RuntimeException {
            String value = valueIn.asString();
            resultOut.setValue(value.toUpperCase());
        }

    }

    public static class Crash extends Node {

        public Crash(NodeLibrary library) {
            super(library);
            setExported(true);
            addPort("value", String.class, Port.Direction.IN);
        }

        @Override
        public void cook(ProcessingContext context) throws RuntimeException {
            int a = 0;
            System.out.println(1 / a);
        }

    }

    public static class TestNetwork extends Macro {

        public TestNetwork(NodeLibrary library) {
            super(library);
            setExported(true);
        }

    }

}
