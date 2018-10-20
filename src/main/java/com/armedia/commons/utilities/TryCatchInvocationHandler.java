package com.armedia.commons.utilities;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class TryCatchInvocationHandler implements InvocationHandler
{
    /**
     * <p>
     * This method is invoked at the very beginning of the <b><code>try-catch-finally</code></b> block, immediately
     * before the <b><code>try</code></b> block is entered.
     * </p>
     *
     * @param proxy
     * @param method
     * @param args
     * @throws Throwable
     */
    protected void onTry(Object proxy, Method method, Object[] args) throws Throwable
    {
        // By default, do nothing
    }

    /**
     * <p>
     * Implementation of {@link InvocationHandler#invoke(Object, Method, Object[])} which uses a
     * <b><code>try-catch-finally</code></b> block with callbacks for each execution branch, for more flexible proxying
     * implementations, but without having to turn to AOP frameworks.
     * </p>
     */
    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        onTry(proxy, method, args);
        try
        {
            return onReturn(proxy, method, args, doInvoke(proxy, method, args));
        }
        catch (final Throwable t)
        {
            Throwable alternate = onCatch(proxy, method, args, t);
            if (alternate == null)
            {
                alternate = t;
            }
            throw alternate;
        }
        finally
        {
            onFinally(proxy, method, args);
        }
    }

    /**
     * <p>
     * Replacement for {@link InvocationHandler#invoke(Object, Method, Object[])}. Implement your core
     * proxy code here. This method is invoked immediately upon entering the <b><code>try</code></b> block of the
     * <b><code>try-catch-finally</code></b>.
     * </p>
     *
     * @param proxy
     * @param method
     * @param args
     * @throws Throwable
     */
    protected abstract Object doInvoke(Object proxy, Method method, Object[] args)
            throws Throwable;

    /**
     * <p>
     * Invoked immediately after {@link #doInvoke(Object, Method, Object[])}, which means it will only be invoked if and
     * only if {@link #doInvoke(Object, Method, Object[])} did not raise an exception. The actual return value is
     * provided as a parameter. This would be the last chance to do anything with that return value before it's actually
     * returned to the caller, or raise an exception. Whatever this method ends up returning will be the actual return
     * value for the overarching {@link #invoke(Object, Method, Object[])} invocation.
     * </p>
     *
     * @param proxy
     * @param method
     * @param args
     * @param returnValue
     * @throws Throwable
     */
    protected Object onReturn(Object proxy, Method method, Object[] args, Object returnValue) throws Throwable
    {
        // By default, do nothing
        return returnValue;
    }

    /**
     * <p>
     * Invoked on the <b><code>catch</code></b> phase of the <b><code>try-catch-finally</code></b> block. The actual
     * exception raised is provided as an argument. The exception is the actual exception that the overarching
     * {@link #invoke(Object, Method, Object[])} invocation will end up raising. However, the method can return another
     * {@link Throwable} instance (the same typing restrictions apply as for
     * {@link InvocationHandler#invoke(Object, Method, Object[])}) to be raised instead of the exception
     * (i.e. unwrapped contained exceptions, replace them, etc).
     * </p>
     * <p>
     * If the method returns <code>null</code>, the original exception is raised.
     * </p>
     *
     * @param proxy
     * @param method
     * @param args
     * @param thrown
     * @return the new exception to raise, or <code>null</code> if no changes are needed
     * @throws Throwable
     */
    protected Throwable onCatch(Object proxy, Method method, Object[] args, Throwable thrown) throws Throwable
    {
        // By default do nothing, just return the original exception
        return thrown;
    }

    /**
     * <p>
     * Invoked during the <b><code>finally</code></b> phase of the <b><code>try-catch-finally</code></b> block.
     * </p>
     *
     * @param proxy
     * @param method
     * @param args
     * @throws Throwable
     */
    protected void onFinally(Object proxy, Method method, Object[] args)
            throws Throwable
    {
        // By default, do nothing
    }
}