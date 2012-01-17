package nodebox.util;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ListUtils {

    /**
     * Get the class of elements of the given list.
     * If a list is null, is empty, or has many different types, returns Object.class.
     *
     * @param objects The list to get.
     * @return the class of all items in the list or Object. Never null.
     */
    public static Class listClass(List objects) {
        if (objects == null || objects.isEmpty()) return Object.class;

        Class c = null;
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            if (i == 0) {
                c = o.getClass();
            } else {
                if (o.getClass() != c) {
                    return Object.class;
                }
            }
        }
        checkNotNull(c);
        return c;
    }

}
