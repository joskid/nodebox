package nodebox.function;


import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import java.util.*;

/**
 * Operations on lists.
 * <p/>
 * When we say lists, we really mean Iterables, the most generic type of lists.
 * We do this because some functions return infinite Iterables.
 * <p/>
 * Functions return the most specific type: for example, sort needs to build up a list from the input Iterable,
 * so it will return this list, avoiding the consumer to create another copy for processing.
 * <p/>
 * Functions here are not concerned what is inside the  items of the list.
 * They operate on the lists themselves, not on the contents of the list elements.
 */
public class ListFunctions {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("list", ListFunctions.class,
                "count",
                "first", "second", "rest", "last",
                "combine", "subList", "shift",
                "reverse", "sort", "shuffle", "pick", "cycle");
    }

    /**
     * Count the number of items in the list.
     * <p/>
     * If the list is infinite, this function will keep on running forever.
     *
     * @param iterable The list items.
     * @return The total amount of items in the list.
     */
    public static long count(Iterable<?> iterable) {
        if (iterable == null) return 0;
        return Iterables.size(iterable);
    }

    /**
     * Take the first item of the list.
     *
     * @param iterable The list items.
     * @return A new list with only the first item.
     */
    public static List<?> first(Iterable<?> iterable) {
        if (iterable == null) return ImmutableList.of();
        Iterator iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return ImmutableList.of(iterator.next());
        }
        return ImmutableList.of();
    }

    /**
     * Take the second item of the list.
     *
     * @param iterable The list items.
     * @return A new list with only the second item.
     */
    public static List<?> second(Iterable<?> iterable) {
        if (iterable == null) return ImmutableList.of();
        Iterator iterator = iterable.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            if (iterator.hasNext()) {
                return ImmutableList.of(iterator.next());
            }
        }
        return ImmutableList.of();
    }

    /**
     * Take all but the first item of the list.
     *
     * @param iterable The list items.
     * @return A new list with the first item skipped.
     */
    public static Iterable<?> rest(Iterable<?> iterable) {
        if (iterable == null) return ImmutableList.of();
        return Iterables.skip(iterable, 1);
    }


    /**
     * Take the last item of the list.
     *
     * @param iterable The list items.
     * @return A new list with only the last item.
     */
    public static List<?> last(Iterable<?> iterable) {
        if (iterable == null) return ImmutableList.of();
        try {
            return ImmutableList.of(Iterables.getLast(iterable));
        } catch (NoSuchElementException e) {
            return ImmutableList.of();
        }
    }

    public static Iterable<?> combine(Iterable list1, Iterable list2, Iterable list3) {
        Iterable<Iterable<?>> nonNullLists = Iterables.filter(Lists.<Iterable<?>>newArrayList(list1, list2, list3), Predicates.notNull());
        return Iterables.concat(nonNullLists);
    }

    /**
     * Combine multiple lists into one.
     *
     * @param iterables The lists to combine.
     * @return A new list with all input lists combined.
     */
    public static Iterable<?> combine(Iterable<?>... iterables) {
        return Iterables.concat(iterables);
    }

    /**
     * Take a slice of the original list.
     *
     * @param iterable   The list items.
     * @param startIndex The starting index, zero-based.
     * @param size       The amount of items.
     * @return A new list containing a slice of the original.
     */
    public static Iterable<?> subList(Iterable<?> iterable, long startIndex, long size) {
        if (iterable == null) return ImmutableList.of();
        Iterable<?> skipped = Iterables.skip(iterable, (int) startIndex);
        return Iterables.limit(skipped, (int) size);
    }

    public static Iterable<?> shift(Iterable<?> iterable, long amount) {
        if (iterable == null) return ImmutableList.of();
        int a = (int) amount % Iterables.size(iterable);
        if (a == 0) return iterable;
        Iterable<?> tail = Iterables.skip(iterable, a);
        Iterable<?> head = Iterables.limit(iterable, a);
        return Iterables.concat(tail, head);
    }

    /**
     * Reverse the items in the list.
     *
     * @param iterable The list items.
     * @return A new list with the items reversed.
     */
    public static List<?> reverse(Iterable<?> iterable) {
        if (iterable == null) return ImmutableList.of();
        return Lists.reverse(ImmutableList.copyOf(iterable));
    }

    /**
     * Sort items in the list according to their natural sort order.
     * <p/>
     * Items need to implement Comparable.
     *
     * @param iterable The list items.
     * @return A new, sorted list.
     */
    public static List<? extends Comparable> sort(Iterable<? extends Comparable> iterable) {
        if (iterable == null) return ImmutableList.of();
        try {
            return ImmutableList.copyOf(Ordering.natural().sortedCopy(iterable));
        } catch (ClassCastException e) {
            // This error occurs when elements of different types are in the list.
            throw new IllegalArgumentException("To sort a list, all elements in the list need to be of the same type.");
        }
    }

    /**
     * Shuffle the items in the list.
     * <p/>
     * Shuffling is stable: using the same seed will always return items in the same sort order.
     *
     * @param iterable The items to shuffle.
     * @param seed     The random seed.
     * @return A new iterable with items in random order.
     */
    public static List<?> shuffle(Iterable<?> iterable, long seed) {
        if (iterable == null) return ImmutableList.of();
        List<?> l = Lists.newArrayList(iterable);
        Collections.shuffle(l, new Random(seed));
        return ImmutableList.copyOf(l);
    }

    /**
     * Pick a number of items from the list.
     * <p/>
     * Shuffling is stable: using the same seed will always return items in the same sort order.
     *
     * @param iterable The items to pick from.
     * @param amount   The amount of items.
     * @param seed     The random seed.
     * @return A new iterable with specified amount of items in random order.
     */
    public static List<?> pick(Iterable<?> iterable, long amount, long seed) {
        if (iterable == null || amount <= 0) return ImmutableList.of();
        List<?> l = Lists.newArrayList(iterable);
        Collections.shuffle(l, new Random(seed));
        if (amount >= l.size()) return ImmutableList.copyOf(l);
        return ImmutableList.copyOf(l.subList(0, (int) amount));
    }

    /**
     * Cycle indefinitely over the elements of the list. This creates an infinite list.
     *
     * @param iterable The list items.
     * @return A new infinite iterable.
     */
    public static Iterable<?> cycle(Iterable<?> iterable) {
        if (iterable == null) return ImmutableList.of();
        return Iterables.cycle(iterable);
    }

}
