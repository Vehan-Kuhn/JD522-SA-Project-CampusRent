
package za.ac.ctu.campusrent.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class GenericUtilities {

    private GenericUtilities() {
    }

    /**
     * Bounded generic method.
     * Finds the largest item in a list.
     */
    public static <T extends Comparable<? super T>> T findMax(
            List<? extends T> list) {

        Objects.requireNonNull(list, "List cannot be null.");

        if (list.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot find maximum of an empty list.");
        }

        T maximum = Objects.requireNonNull(
                list.get(0), "List cannot contain null values.");

        for (T item : list) {
            Objects.requireNonNull(
                    item, "List cannot contain null values.");

            if (item.compareTo(maximum) > 0) {
                maximum = item;
            }
        }

        return maximum;
    }

    /**
     * PECS example:
     * Producer Extends: source produces T values.
     * Consumer Super: destination consumes T values.
     */
    public static <T> void copyAll(
            Collection<? extends T> source,
            Collection<? super T> destination) {

        Objects.requireNonNull(source);
        Objects.requireNonNull(destination);

        for (T item : source) {
            destination.add(item);
        }
    }

    /**
     * Creates a chained comparator.
     */
    public static <T, U extends Comparable<? super U>,
            V extends Comparable<? super V>>
    Comparator<T> chainedComparator(
            java.util.function.Function<T, U> first,
            java.util.function.Function<T, V> second) {

        return Comparator.comparing(first)
                .thenComparing(second);
    }
}
