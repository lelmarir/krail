package uk.q3c.krail.core.shiro;

public interface SecuritySession {
	
	/**
	 * A unique id that rapresent the session. Will be used in identity (==) comparisons.
	 */
	public Object getSessionId();
	
	/**
	 * Returns the object bound with the specified name in this session, or
	 * <code>null</code> if no object is bound under the name.
	 *
	 * @param name
	 *            a string specifying the name of the object
	 *
	 * @return the object with the specified name
	 *
	 * @exception IllegalStateException
	 *                if this method is called on an invalidated session
	 */
	public Object getAttribute(String name);

	/**
	 * Binds an object to this session, using the name specified. If an object
	 * of the same name is already bound to the session, the object is replaced.
	 *
	 * <p>
	 * After this method executes, and if the new object implements
	 * <code>HttpSessionBindingListener</code>, the container calls
	 * <code>HttpSessionBindingListener.valueBound</code>. The container then
	 * notifies any <code>HttpSessionAttributeListener</code>s in the web
	 * application.
	 * 
	 * <p>
	 * If an object was already bound to this session of this name that
	 * implements <code>HttpSessionBindingListener</code>, its
	 * <code>HttpSessionBindingListener.valueUnbound</code> method is called.
	 *
	 * <p>
	 * If the value passed in is null, this has the same effect as calling
	 * <code>removeAttribute()<code>.
	 *
	 *
	 * @param name
	 *            the name to which the object is bound; cannot be null
	 *
	 * @param value
	 *            the object to be bound
	 *
	 * @exception IllegalStateException
	 *                if this method is called on an invalidated session
	 */
	public void setAttribute(String name, Object value);

}
