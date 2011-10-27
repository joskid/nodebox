package nodebox.node;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

final class PrototypeMap {

    public static final PrototypeMap ROOT = new PrototypeMap(null, ImmutableMap.<String, Object>of());

    private final PrototypeMap prototype;
    private final ImmutableMap<String, Object> properties;

    public PrototypeMap(@Nullable PrototypeMap prototype, Map<String, Object> properties) {
        checkNotNull(properties);
        this.prototype = prototype;
        this.properties = ImmutableMap.copyOf(properties);
    }

    public PrototypeMap(@Nullable PrototypeMap prototype, ImmutableMap<String, Object> properties) {
        checkNotNull(properties);
        this.prototype = prototype;
        this.properties = properties;
    }

    public PrototypeMap getPrototype() {
        return prototype;
    }

    public int size() {
        return properties.size();
    }

    public Object getProperty(String key) {
        if (hasOwnProperty(key)) {
            return properties.get(key);
        } else {
            return prototype != null ? prototype.getProperty(key) : null;
        }
    }

    public ImmutableMap<String, Object> getProperties() {
        return properties;
    }

    public boolean hasProperty(String key) {
        checkNotNull(key);
        return properties.containsKey(key) || prototype != null && prototype.hasProperty(key);
    }

    public boolean hasOwnProperty(String key) {
        checkNotNull(key);
        return properties.containsKey(key);
    }

    //// Mutation methods ////

    public PrototypeMap extend() {
        return new PrototypeMap(this, ImmutableMap.<String, Object>of());
    }

    public PrototypeMap withProperty(String key, Object value) {
        checkNotNull(key, "Key cannot be null.");
        checkNotNull(value, "Value cannot be null.");
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        // ImmutableMap.Builder doesn't want duplicate keys.
        // Skip over the entry with the key you want to change.
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!entry.getKey().equals(key)) {
                builder.put(entry.getKey(), entry.getValue());
            }
        }
        builder.put(key, value);
        return new PrototypeMap(prototype, builder.build());
    }

    public PrototypeMap withProperties(Map<String, Object> properties) {
        checkNotNull(properties, "Properties cannot be null.");
        return new PrototypeMap(prototype, ImmutableMap.copyOf(properties));
    }

}
