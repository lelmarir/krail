/*
 * Copyright (c) 2015. David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package uk.q3c.krail.core.shiro;

import com.google.inject.Inject;
import com.vaadin.server.VaadinSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

/**
 * A {@link Subject} is not stored directly in a {@link VaadinSession}, but rather its principals.
 * These are then retrieved by a subsequent call to {@link #get()}, and a new Subject instance created by {@link Subject.Builder} from those principals
 * <p>
 * <p>
 * Created by David Sowerby on 10/06/15.
 */
public class DefaultSubjectProvider implements SubjectProvider {


    /**
     * The security manager for the application.
     */
    private SecurityManager securityManager;

    @Inject
    protected DefaultSubjectProvider(SecurityManager securityManager) {
        super();
        this.securityManager = securityManager;
    }

    /**
     * Sets the security manager for the application. To support push, normally a
     * {@link DefaultSecurityManager} is used rather than a web specific one
     * because the normal HTTP request/response cycle isn't used.
     *
     * @param securityManager the security manager to set
     */
    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subject get() {
        return getSubject();
    }

    /**
     * Returns the subject for the application and thread which represents the
     * current user. The subject is always available; however it may represent an
     * anonymous user.
     *
     * @return the subject for the current application and thread
     * @see SecurityUtils#getSubject()
     */
    public Subject getSubject() {
        VaadinSession session = VaadinSession.getCurrent();

        // This should never happen, but just in case we'll check.
        if (session == null) {
            throw new IllegalStateException("Unable to locate VaadinSession to store Shiro Subject.");
        }

        PrincipalCollection principals = (PrincipalCollection) session.getAttribute(SUBJECT_ATTRIBUTE);
        Subject.Builder builder = new Subject.Builder(securityManager);
        Subject subject;


        if (principals != null) {
            // use existing principals
            builder.principals(principals);
        }
        subject = builder.buildSubject();
        try {
            session.getLockInstance().lock();
            session.setAttribute(SUBJECT_ATTRIBUTE, subject.getPrincipals());
        } finally {
            session.getLockInstance().unlock();
        }

        return subject;
    }
}
