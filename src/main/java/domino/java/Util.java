package domino.java;

import java.util.LinkedHashMap;

import org.osgi.framework.Constants;

import de.tototec.utils.functional.Optional;

public interface Util {

	/**
	 * Creates a filter criteria expression which matches the given main type and
	 * the given custom filter. Thus, it includes the main `OBJECTCLASS` filter
	 * criteria.
	 *
	 * @param tpe
	 *            Type information
	 * @param customFilter
	 *            A custom filter expression
	 */
	public static String createCompleteFilter(final Class<?> type, final String customFilter) {
		final String className = type.getName();
		final String objectClassFilter = createObjectClassFilter(className);

		// Combine
		return linkFiltersWithAnd(Optional.some(objectClassFilter), Optional.lift(customFilter)).get();
	}

	/**
	 * Creates an `OBJECTCLASS` filter for the given class.
	 */
	public static String createObjectClassFilter(final String typeName) {
		return "(" + Constants.OBJECTCLASS + "=" + typeName + ")";
	}

	/**
	 * Links to filter expressions with a logical AND if both are given, otherwise
	 * returns just one of it.
	 *
	 * @param filterOne
	 *            First filter
	 * @param filterTwo
	 *            Second filter
	 * @return result
	 */
	public static Optional<String> linkFiltersWithAnd(final Optional<String> filterOne,
			final Optional<String> filterTwo) {
		// TODO Do this more elegantly
		if (filterOne.isDefined()) {
			if (filterTwo.isDefined()) {
				return Optional.some("(&" + filterOne.get() + filterTwo.get() + ")");
			} else {
				return filterOne;
			}
		} else {
			return filterTwo;
		}
	}

	/**
	 * Convenience method to create a {@link LinkedHashMap}<K, V> with 1 entry
	 * in-place.
	 * 
	 * @return The created Map.
	 */
	public static <K, V> LinkedHashMap<K, V> asMap(K key1, V value1) {
		LinkedHashMap<K, V> linkedHashMap = new LinkedHashMap<>();
		linkedHashMap.put(key1, value1);
		return linkedHashMap;
	}

	/**
	 * Convenience method to create a {@link LinkedHashMap}<K, V> with 2 entries
	 * in-place.
	 * 
	 * @return The created Map.
	 */
	public static <K, V> LinkedHashMap<K, V> asMap(K key1, V value1, K key2, V value2) {
		LinkedHashMap<K, V> linkedHashMap = new LinkedHashMap<>();
		linkedHashMap.put(key1, value1);
		linkedHashMap.put(key2, value2);
		return linkedHashMap;
	}

	/**
	 * Convenience method to create a {@link LinkedHashMap}<K, V> with 3 entries
	 * in-place.
	 * 
	 * @return The created Map.
	 */
	public static <K, V> LinkedHashMap<K, V> asMap(
			K key1, V value1,
			K key2, V value2,
			K key3, V value3) {
		LinkedHashMap<K, V> linkedHashMap = new LinkedHashMap<>();
		linkedHashMap.put(key1, value1);
		linkedHashMap.put(key2, value2);
		linkedHashMap.put(key3, value3);
		return linkedHashMap;
	}

	/**
	 * Convenience method to create a {@link LinkedHashMap}<K, V> with 4 entries
	 * in-place.
	 * 
	 * @return The created Map.
	 */
	public static <K, V> LinkedHashMap<K, V> asMap(
			K key1, V value1,
			K key2, V value2,
			K key3, V value3,
			K key4, V value4) {
		LinkedHashMap<K, V> linkedHashMap = new LinkedHashMap<>();
		linkedHashMap.put(key1, value1);
		linkedHashMap.put(key2, value2);
		linkedHashMap.put(key3, value3);
		linkedHashMap.put(key4, value4);
		return linkedHashMap;
	}
}
