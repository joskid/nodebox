package nodebox.node;

public final class MenuItem {

    private final String key;
    private final String label;

    public MenuItem(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuItem menuItem = (MenuItem) o;

        if (!key.equals(menuItem.key)) return false;
        if (!label.equals(menuItem.label)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + label.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("[%s: %s]", key, label);
    }
}

