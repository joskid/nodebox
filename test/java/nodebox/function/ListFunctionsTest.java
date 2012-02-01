package nodebox.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class ListFunctionsTest {

    @Test
    public void testFirst() {
        assertElements(ListFunctions.first(ImmutableList.of()));
        assertElements(ListFunctions.first(ImmutableList.of(1)), 1);
        assertElements(ListFunctions.first(ImmutableList.of(1, 2)), 1);
        assertElements(ListFunctions.first(ImmutableList.of(1, 2, 3)), 1);
    }

    @Test
    public void testSecond() {
        assertElements(ListFunctions.second(ImmutableList.of()));
        assertElements(ListFunctions.second(ImmutableList.of(1)));
        assertElements(ListFunctions.second(ImmutableList.of(1, 2)), 2);
        assertElements(ListFunctions.second(ImmutableList.of(1, 2, 3)), 2);
    }

    @Test
    public void testRest() {
        assertElements(ListFunctions.rest(ImmutableList.of()));
        assertElements(ListFunctions.rest(ImmutableList.of(1)));
        assertElements(ListFunctions.rest(ImmutableList.of(1, 2)), 2);
        assertElements(ListFunctions.rest(ImmutableList.of(1, 2, 3)), 2, 3);
    }

    @Test
    public void testLast() {
        assertElements(ListFunctions.last(ImmutableList.of()));
        assertElements(ListFunctions.last(ImmutableList.of(1)), 1);
        assertElements(ListFunctions.last(ImmutableList.of(1, 2)), 2);
        assertElements(ListFunctions.last(ImmutableList.of(1, 2, 3)), 3);
    }

    @Test
    public void testCombine() {
        assertElements(ListFunctions.combine(ImmutableList.of(), ImmutableList.of()));
        assertElements(ListFunctions.combine(ImmutableList.of(1), ImmutableList.of()), 1);
        assertElements(ListFunctions.combine(ImmutableList.of(1), ImmutableList.of(2)), 1, 2);
    }

    @Test
    public void testSubList() {
        assertElements(ListFunctions.subList(ImmutableList.of(1, 2, 3, 4), 0, 100), 1, 2, 3, 4);
        assertElements(ListFunctions.subList(ImmutableList.of(), 100, 100));
        assertElements(ListFunctions.subList(ImmutableList.of(1, 2, 3, 4), 1, 2), 2, 3);
        assertElements(ListFunctions.subList(ImmutableList.of(1, 2, 3, 4), 100, 2));
    }

    @Test
    public void testReverse() {
        assertElements(ListFunctions.reverse(ImmutableList.of()));
        assertElements(ListFunctions.reverse(ImmutableList.of(1)), 1);
        assertElements(ListFunctions.reverse(ImmutableList.of(1, 2)), 2, 1);
        assertElements(ListFunctions.reverse(ImmutableList.of(1, 2, 3)), 3, 2, 1);
    }

    @Test
    public void testSort() {
        assertElements(ListFunctions.sort(ImmutableList.of("c", "b", "a")), "a", "b", "c");
        assertElements(ListFunctions.sort(ImmutableList.of(9, 3, 5)), 3, 5, 9);
    }

    @Test
    public void testShuffle() {
        // Shuffling is stable: the same seed always returns the same sort order.
        assertElements(ListFunctions.shuffle(ImmutableList.of(), 42));
        assertElements(ListFunctions.shuffle(ImmutableList.of(1), 42), 1);
        assertElements(ListFunctions.shuffle(ImmutableList.of(1, 2, 3, 4, 5), 42), 2, 3, 4, 5, 1);
        assertElements(ListFunctions.shuffle(ImmutableList.of(1, 2, 3, 4, 5), 33), 2, 1, 5, 3, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSortDisparateElements() {
        // You can't sort elements of different types. This error is caught and wrapped in an illegal argument exception.
        ListFunctions.sort(ImmutableList.of("hello", 42, 15.0));
    }

    @Test
    public void testCycle() {
        assertFirstElements(ListFunctions.cycle(ImmutableList.of()));
        assertFirstElements(ListFunctions.cycle(ImmutableList.of(1)), 1, 1, 1, 1, 1);
        assertFirstElements(ListFunctions.cycle(ImmutableList.of(1, 2)), 1, 2, 1, 2, 1);
        assertFirstElements(ListFunctions.cycle(ImmutableList.of(1, 2, 3)), 1, 2, 3, 1, 2);
    }

    private void assertElements(Iterable<?> iterable, Object... items) {
        assertEquals(ImmutableList.copyOf(iterable), ImmutableList.copyOf(items));
    }

    private void assertFirstElements(Iterable<?> iterable, Object... items) {
        assertElements(Iterables.limit(iterable, items.length), items);
    }

}
