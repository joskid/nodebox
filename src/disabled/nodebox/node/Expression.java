/*
 * This file is part of NodeBox.
 *
 * Copyright (C) 2008 Frederik De Bleser (frederik@pandora.be)
 *
 * NodeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NodeBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NodeBox. If not, see <http://www.gnu.org/licenses/>.
 */
package nodebox.node;

import nodebox.graphics.Color;
import nodebox.util.waves.AbstractWave;
import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.UnresolveablePropertyException;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.impl.BaseVariableResolverFactory;
import org.mvel2.integration.impl.SimpleValueResolver;
import org.mvel2.optimizers.OptimizerFactory;
import org.omg.Dynamic.Parameter;
import org.python.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Expression {

    static ParserContext parserContext = new ParserContext();
    private static ImmutableMap<String, VariableResolver> defaultResolvers;

    static {
        // Initialize MVEL.

        // The dynamic optimizer crashes for some reason, so we use the "safe reflective" one.
        // Although "safe" sounds slower, this optimizer actually seems *faster*
        // than the dynamic one. Don't change this unless you want to go digging for weird
        // reflective constructor errors.
        OptimizerFactory.setDefaultOptimizer(OptimizerFactory.SAFE_REFLECTIVE);

        // Add "built-in" methods to the expression context.
        parserContext = new ParserContext();
        try {
            // MVEL has a bug where it accepts methods with varargs, but only executes the method with
            // non-varargs. So in our ExpressionHelper we have both the varargs and non-varargs methods.
            // We lookup the varargs version here, but only the non-varargs will get called.
            parserContext.addImport("random", ExpressionHelper.class.getMethod("random", Object.class, double[].class));
            parserContext.addImport("randint", ExpressionHelper.class.getMethod("randint", Object.class, int.class, int.class));
            parserContext.addImport("clamp", ExpressionHelper.class.getMethod("clamp", double.class, double.class, double.class));
            parserContext.addImport("color", ExpressionHelper.class.getMethod("color", double[].class));
            parserContext.addImport("rgb", ExpressionHelper.class.getMethod("color", double[].class));
            parserContext.addImport("hsb", ExpressionHelper.class.getMethod("hsb", double[].class));
            parserContext.addImport("stamp", ExpressionHelper.class.getMethod("stamp", String.class, Object.class));
            parserContext.addImport("int", ExpressionHelper.class.getMethod("toInt", double.class));
            parserContext.addImport("float", ExpressionHelper.class.getMethod("toFloat", int.class));
            parserContext.addImport("hold", ExpressionHelper.class.getMethod("hold", double.class, double.class, double[].class));
            parserContext.addImport("schedule", ExpressionHelper.class.getMethod("schedule", double.class, double.class, double.class, double[].class));
            parserContext.addImport("timeloop", ExpressionHelper.class.getMethod("timeloop", double.class, List.class));
            parserContext.addImport("timeloop", ExpressionHelper.class.getMethod("timeloop", double.class, List.class, double.class));
            parserContext.addImport("wave", ExpressionHelper.class.getMethod("wave"));
            parserContext.addImport("wave", ExpressionHelper.class.getMethod("wave", AbstractWave.Type.class, double[].class));
            parserContext.addImport("math", Math.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Unknown static method for expression." + e);
        }

        ImmutableMap.Builder<String, VariableResolver> resolvers = ImmutableMap.builder();
        for (AbstractWave.Type wave : AbstractWave.Type.values())
            resolvers.put(wave.name(), new SimpleValueResolver(wave));
        defaultResolvers = resolvers.build();
    }

    private final Port port;
    private final String expression;
    private transient Throwable error;
    private transient Serializable compiledExpression;
    private Set<WeakReference<Port>> referencedPorts;

    /**
     * Construct and set the expression.
     * <p/>
     * The expression is accepted as is, and no errors will be thrown even if the expression is invalid.
     * Only during compilation or evaluation will this result in an error.
     *
     * @param port
     * @param expression
     */
    public Expression(Port port, String expression) {
        assert port != null; // We need the current port for stamp expressions.
        this.port = port;
        this.expression = expression;
        referencedPorts = null;
        compiledExpression = null;
    }

    //// Attribute access ////

    public String getExpression() {
        return expression;
    }

    public boolean hasError() {
        return error != null;
    }

    public Throwable getError() {
        return error;
    }

    /* package private */

    void setError(Exception error) {
        // This method is called from Parameter to set an error for cyclic dependencies.
        this.error = error;
    }

    public Port getPort() {
        return port;
    }

    //// Values ////

    public boolean asBoolean() throws ExpressionError {
        Object value = evaluate();
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            throw new ExpressionError("Value \"" + value + "\" for expression \"" + expression + "\" is not a boolean.");
        }
    }

    public int asInt() throws ExpressionError {
        Object value = evaluate();
        if (value instanceof Number) {
            return (Integer) value;
        } else {
            throw new ExpressionError("Value \"" + value + "\" for expression \"" + expression + "\" is not an integer.");
        }
    }

    public double asFloat() throws ExpressionError {
        Object value = evaluate();
        if (value instanceof Number) {
            return (Double) value;
        } else {
            throw new ExpressionError("Value \"" + value + "\" for expression \"" + expression + "\" is not a floating-point value.");
        }
    }

    public String asString() throws ExpressionError {
        Object value = evaluate();
        if (value instanceof String) {
            return (String) value;
        } else {
            throw new ExpressionError("Value \"" + value + "\" for expression \"" + expression + "\" is not a string.");
        }
    }

    public Color asColor() throws ExpressionError {
        Object value = evaluate();
        if (value instanceof Color) {
            return (Color) value;
        } else {
            throw new ExpressionError("Value \"" + value + "\" for expression \"" + expression + "\" is not a color.");
        }
    }

    //// Evaluation ////

    /**
     * Compile the expression.
     *
     * @throws ExpressionError if the compilation fails.
     * @see #getError()
     */
    public void compile() throws ExpressionError {
        try {
            this.compiledExpression = MVEL.compileExpression(expression, parserContext);
            error = null;
        } catch (Exception e) {
            error = e;
            throw new ExpressionError("Cannot compile expression '" + expression + "' on " + getPort() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Evaluate the expression and return the result.
     * <p/>
     * Throw an exception if an error occurs. You can retrieve this exception by calling getError().
     *
     * @param context the context wherein evaluation happens.
     * @param parent The parent node this expression is evaluated in.
     * @param node The current node this port expression is on.
     * @return the result of the expression
     * @throws ExpressionError if an error occurs whilst evaluating the expression.
     * @see #getError()
     */
    public Object evaluate(NodeContext context, Node parent, Node node) throws ExpressionError {
        // If there was an error with the expression, throw it before doing anything.
        if (hasError()) {
            throw new ExpressionError("Cannot compile expression '" + expression + "' on " + parent + ": " + getError().getMessage(), getError());
        }

        // If the expression was not compiled, compile it first.
        // This can throw an ExpressionError, which will be forwarded to the caller.
        if (compiledExpression == null) {
            compile();
        }
        // Set up state variables in the expression utilities class.
        // TODO: This is not thread-safe.
        ExpressionHelper.currentContext = context;
        ExpressionHelper.currentPort = port;
        // Marked port references are used to find which parameters this expression references.
        referencedPorts = new HashSet<WeakReference<Port>>();
        ProxyResolverFactory prf = new ProxyResolverFactory(node, context, referencedPorts);
        try {
            error = null;
            return MVEL.executeExpression(compiledExpression, prf);
        } catch (Exception e) {
            error = e;
            throw new ExpressionError("Cannot evaluate expression '" + expression + "' on " + getPort().getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns all parameters this expression depends on
     * <p/>
     * If the expression contains an error, this method will return an empty set.
     *
     * @return a set of parameters
     */
    public Set<Parameter> getDependencies() {
        if (referencedPorts == null) {
            try {
                evaluate();
            } catch (ExpressionError expressionError) {
                return new HashSet<Parameter>(0);
            }
        }
        HashSet<Parameter> dependencies = new HashSet<Parameter>(referencedPorts.size());
        for (WeakReference<Parameter> ref : referencedPorts) {
            Parameter p = ref.get();
            if (p != null)
                dependencies.add(p);
        }
        return dependencies;
    }

    class ProxyResolverFactory extends BaseVariableResolverFactory {

        private Node node;
        private NodeAccessProxy proxy;
        private ProcessingContext context;


        public ProxyResolverFactory(Node node, ProcessingContext context) {
            this.node = node;
            proxy = new NodeAccessProxy(node);
            this.context = context;
            variableResolvers.putAll(defaultResolvers);
        }

        public ProxyResolverFactory(Node node, ProcessingContext context, Set<WeakReference<Parameter>> markedParameterReferences) {
            this.node = node;
            proxy = new NodeAccessProxy(node, markedParameterReferences);
            this.context = context;
            variableResolvers.putAll(defaultResolvers);
        }

        public Node getNode() {
            return node;
        }

        public NodeAccessProxy getProxy() {
            return proxy;
        }

        public VariableResolver createVariable(String name, Object value) {
            throw new CompileException("Variable assignment is not supported.");
        }

        public VariableResolver createVariable(String name, Object value, Class<?> type) {
            throw new CompileException("Variable assignment is not supported.");
        }

        @Override
        public VariableResolver getVariableResolver(String name) {
            if (variableResolvers == null) {
                variableResolvers = new HashMap<String, VariableResolver>();
                variableResolvers.putAll(defaultResolvers);
            }
            VariableResolver vr = variableResolvers.get(name);
            if (vr != null) {
                return vr;
            } else if (proxy.containsKey(name)) {
                vr = new ProxyResolver(proxy, proxy.get(name));
                variableResolvers.put(name, vr);
                return vr;
            } else if (context.containsKey(name)) {
                vr = new ProcessingContextResolver(context, name);
                variableResolvers.put(name, vr);
                return vr;
            } else if (nextFactory != null) {
                return nextFactory.getVariableResolver(name);
            }
            throw new UnresolveablePropertyException("unable to resolve variable '" + name + "'");
        }

        public boolean isResolveable(String name) {
            return (variableResolvers != null && variableResolvers.containsKey(name))
                    || (proxy.containsKey(name))
                    || (context.containsKey(name))
                    || (nextFactory != null && nextFactory.isResolveable(name));
        }

        public boolean isTarget(String name) {
            return variableResolvers != null && variableResolvers.containsKey(name);
        }

        @Override
        public Set<String> getKnownVariables() {
            Set<String> knownVariables = new HashSet<String>();
            knownVariables.addAll(proxy.keySet());
            knownVariables.addAll(context.keySet());
            return knownVariables;
        }
    }

    class ProxyResolver implements VariableResolver {

        private NodeAccessProxy proxy;
        private Object value;

        public ProxyResolver(NodeAccessProxy proxy, Object value) {
            this.proxy = proxy;
            this.value = value;
        }

        public NodeAccessProxy getProxy() {
            return proxy;
        }

        public Node getNode() {
            return proxy.getNode();
        }

        public String getName() {
            return proxy.getNode().getName();
        }

        public Class getType() {
            return Object.class;
        }

        public void setStaticType(Class type) {
            throw new RuntimeException("Not implemented");
        }

        public int getFlags() {
            return 0;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            throw new CompileException("Parameter values cannot be changed through expressions.");
        }
    }

    class ProcessingContextResolver implements VariableResolver {

        private String name;
        private ProcessingContext context;

        ProcessingContextResolver(ProcessingContext context, String name) {
            this.context = context;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Class getType() {
            return Object.class;
        }

        public void setStaticType(Class aClass) {
            throw new RuntimeException("Not implemented");
        }

        public int getFlags() {
            return 0;
        }

        public Object getValue() {
            return context.get(name);
        }

        public void setValue(Object o) {
            throw new CompileException("You cannot change the value of a constant.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Expression other = (Expression) o;
        return expression.equals(other.expression);
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }
}
