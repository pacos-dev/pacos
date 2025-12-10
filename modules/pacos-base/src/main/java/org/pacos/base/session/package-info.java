/**
 * Contains session objects
 * <p>
 *     Example of usage
 * </p>
 * <code>
 *     //get information about logged user
 *     UserSession.getCurrent();
 *
 *     //add object to user session
 *     UserSession.getCurrent().addToSession("key",myOject);
 *
 *     //get object from session
 *     UserSession.getCurrent().getFromSession("key");
 *
 *     //remove object from session
 *     UserSession.getCurrent().removeFromSession("key");
 * </code>
 */
package org.pacos.base.session;