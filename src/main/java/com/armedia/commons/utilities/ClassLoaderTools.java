/**
 *
 */

package com.armedia.commons.utilities;

/**
 * @author diego
 *
 */
public class ClassLoaderTools {

	public static <T extends Object> Class<T> locateClass(String name, Class<T> primary, Class<?>... requiredSubclasses)
		throws ClassCastException {
		final Class<?> c;
		try {
			c = Class.forName(name);
		} catch (ClassNotFoundException e) {
			// Nothing found...
			return null;
		}

		if (!primary.isAssignableFrom(c)) {
			// Not assignable... fail...
			throw new ClassCastException(
				String.format("Class %s is not assignable as a %s", c.getCanonicalName(), primary.getCanonicalName()));
		}

		@SuppressWarnings("unchecked")
		Class<T> k = (Class<T>) c;

		// Ok...so we have the class...does it match ALL the requirements?
		for (Class<?> r : requiredSubclasses) {
			if (r == null) {
				continue;
			}
			if (!r.isAssignableFrom(k)) {
				// Not assignable... fail...
				throw new ClassCastException(
					String.format("Class %s is not assignable as a %s", k.getCanonicalName(), r.getCanonicalName()));
			}
		}
		return k;
	}
}