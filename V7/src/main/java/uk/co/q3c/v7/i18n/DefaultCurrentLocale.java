/*
 * Copyright (C) 2013 David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.co.q3c.v7.i18n;

import com.google.inject.Inject;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.WebBrowser;

import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.q3c.v7.base.guice.vsscope.VaadinSessionScoped;
import uk.co.q3c.v7.base.shiro.SubjectProvider;
import uk.co.q3c.v7.base.shiro.loginevent.AuthenticationEvent.AuthenticationListener;
import uk.co.q3c.v7.base.shiro.loginevent.AuthenticationEvent.AuthenticationNotifier;
import uk.co.q3c.v7.base.shiro.loginevent.AuthenticationEvent.FailedLoginEvent;
import uk.co.q3c.v7.base.shiro.loginevent.AuthenticationEvent.LogoutEvent;
import uk.co.q3c.v7.base.shiro.loginevent.AuthenticationEvent.SuccesfulLoginEvent;
import uk.co.q3c.v7.base.ui.BrowserProvider;
import uk.co.q3c.v7.base.user.opt.UserOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * When a CurrentLocale is instantiated it sets the current locale according to the following priorities: <ol>
 * <li>If a user is authenticated, the UserOption for preferred locale is used, if valid</li>
 * <li>If a user is not logged in, or the user option was invalid, the browser locale is used</li>
 * <li>If the browser locale not accessible, or is not a supported locale (as defined in {@link I18NModule} or its
 * sub-class), the first supported locale is used.</li>
 * </ol>
 * When a user logs in after initialisation, the UserOption value for preferred locale is used.
 * When a user logs out, no change is made, as the user may still have public pages they can view.
 * <p/>
 * Scope for this class must be set in {@link I18NModule} or its sub-class - this enables the developer to choose
 * between {@link UIScoped} or {@link VaadinSessionScoped}, depending on whether they want their users to set the
 * language for each browser tab or each browser instance, respectively
 *
 * @author David Sowerby
 * @date 5 May 2014
 */

public class DefaultCurrentLocale implements CurrentLocale, AuthenticationListener {
    private static Logger log = LoggerFactory.getLogger(DefaultCurrentLocale.class);
    private final List<LocaleChangeListener> listeners = new ArrayList<>();
    private BrowserProvider browserProvider;
    private Locale defaultLocale;
    private Locale locale;
    private Set<Locale> supportedLocales;
    private UserOption userOption;
	private SubjectProvider subjectProvider;

    @Inject
    protected DefaultCurrentLocale(BrowserProvider browserProvider, @SupportedLocales Set<Locale> supportedLocales,
                                   @DefaultLocale Locale defaultLocale, UserOption userOption, SubjectProvider subjectProvider, AuthenticationNotifier authNotifier) {
        super();
        this.browserProvider = browserProvider;
        this.supportedLocales = supportedLocales;
        this.defaultLocale = defaultLocale;
        this.userOption = userOption;
        this.subjectProvider = subjectProvider;
        authNotifier.addListener(this);
        initialise();
    }

    /**
     * Sets up the locale, see the Javadoc for the class
     */
    private void initialise() {
        if (setLocaleFromUserOption(false)) {
            return;
        }
        if (setLocaleFromBrowser(false)) {
            return;
        }
        setLocale(defaultLocale, false);
    }

    /**
     * Sets locale to the browser locale, if available.  Browser locale will not be available if the browser is not
     * active ( this usually only happens in testing or background tasks)
     *
     * @param fireListeners
     *
     * @return true if the browser was accessible and its locale is supported
     */
    private boolean setLocaleFromBrowser(boolean fireListeners) {
        WebBrowser webBrowser = browserProvider.get();
        if (webBrowser != null) {
            Locale browserLocale = webBrowser.getLocale();
            if (supportedLocales.contains(browserLocale)) {
                setLocale(browserLocale, fireListeners);
                return true;
            } else {
                locale = defaultLocale;
            }
        }
        return false;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        setLocale(locale, true);
    }

    @Override
    public void setLocale(Locale locale, boolean fireListeners) {
        if (supportedLocales.contains(locale)) {

            if (locale != this.locale) {
                this.locale = locale;
                Locale.setDefault(locale);
                log.debug("CurrentLocale set to {}", locale);
                if (fireListeners) {
                    fireListeners(locale);
                }
            }
        } else {
            throw new UnsupportedLocaleException("Locale is not supported: " + locale);
        }

    }

    private void fireListeners(Locale locale) {
        for (LocaleChangeListener listener : listeners) {
            listener.localeChanged(locale);
        }
    }

    @Override
    public void addListener(LocaleChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(LocaleChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void removeAllListeners() {
        listeners.clear();

    }
    
    public void userStatusChanged() {
        if (subjectProvider.get().isAuthenticated()) {
            setLocaleFromUserOption(true);
        }
    }
    
    @Override
	public void onSuccess(SuccesfulLoginEvent event) {
    	userStatusChanged();
	}

	@Override
	public void onFailure(FailedLoginEvent event) {
		;
	}

	@Override
	public void onLogout(LogoutEvent event) {
		userStatusChanged();
	}

    /**
     * Sets the locale from the value held in UserOption, if available.  UserOption will not be available if the user
     * is not authenticated.  It is possible that a user option is not supported (unlikely, but support for a language
     * could be withdrawn after the user has chosen it), in which case the locale is set to the first supported locale
     * @param subject 
     *
     * @param fireListeners
     *
     * @return true if the user options was valid, otherwise false
     */
    private boolean setLocaleFromUserOption(boolean fireListeners) {
        if (subjectProvider.get().isAuthenticated()) {
            String preferredLocale = userOption.getOptionAsString(DefaultCurrentLocale.class.getSimpleName(),
                    "preferredLocale", defaultLocale.toLanguageTag());
            Locale selectedLocale = Locale.forLanguageTag(preferredLocale);
            if (supportedLocales.contains(selectedLocale)) {
                setLocale(selectedLocale, fireListeners);
                return true;
            }
        }
        return false;
    }
}
