package com.armedia.commons.utilities;

/**
 * This interface allows for an easy callback mechanism to facilitate the customized selection of
 * services via {@link PluggableServiceLocator}.
 *
 * @author diego.rivera@armedia.com
 *
 * @param <S>
 */
public interface PluggableServiceSelector<S> {

	/**
	 * Returns {@code true} if the service meets the required criteria, {@code false} otherwise. The
	 * {@code service} parameter will <b>never</b> be {@code null} when invoked via
	 * {@link PluggableServiceLocator}.
	 *
	 * @param service
	 * @return {@code true} if the service meets the required criteria, {@code false} otherwise
	 */
	public boolean matches(S service);

}