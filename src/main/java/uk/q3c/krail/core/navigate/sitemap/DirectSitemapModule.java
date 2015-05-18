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
package uk.q3c.krail.core.navigate.sitemap;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import uk.q3c.krail.core.guice.DefaultBindingManager;
import uk.q3c.krail.core.shiro.PageAccessControl;
import uk.q3c.krail.core.view.KrailView;
import uk.q3c.krail.i18n.I18NKey;

/**
 * If you want to create Sitemap entries for your own code using a direct coding approach, you can either subclass this
 * module and provide the entries in the {@link #define} method, or just simply use this as an example and create your
 * own. The module then needs to be added to your subclass of {@link DefaultBindingManager}. By convention, modules
 * relating to the Sitemap are added in the addSitemapModules() method.
 * <p>
 * You can add any number of modules this way, but any duplicated map keys (the URI segments) will cause the map
 * injection to fail. There is an option to change this behaviour in MapBinder#permitDuplicates()
 * <p>
 * You can use multiple subclasses of this, Guice will merge all of the bindings into a single MapBinder<String,
 * DirectSitemapEntry> for use by the {@link DirectSitemapLoader}
 *
 * @author David Sowerby
 */
public abstract class DirectSitemapModule extends AbstractModule {

    private MapBinder<String, RedirectEntry> redirectBinder;
    private MapBinder<String, DirectSitemapEntry> sitemapBinder;

    @Override
    protected void configure() {
        this.sitemapBinder = MapBinder.newMapBinder(binder(), String.class, DirectSitemapEntry.class);
        redirectBinder = MapBinder.newMapBinder(binder(), String.class, RedirectEntry.class);
        define();
    }

    /**
     * Override this method to define {@link MasterSitemap} entries with one or more calls to {@link #addEntry},
     * something
     * like this:
     * <p>
     * addEntry("public/home", PublicHomeView.class, LabelKey.Home, false, "permission");
     * <p>
     * and redirects with {@link #addRedirect(String, String)}
     *
     * @see #addEntry(String, Class, I18NKey, PageAccessControl)
     */
    protected abstract void define();

    protected void addEntry(String uri, Class<? extends KrailView> viewClass, I18NKey labelKey, PageAccessControl pageAccessControl) {
        addEntry(uri, viewClass, labelKey, pageAccessControl, null);
    }

    /**
     * Adds an entry to be place in the {@link MasterSitemap} by the {@link DirectSitemapLoader}. Defaults the position index to 1
     *
     * @param uri
     *         the URI for this page
     * @param viewClass
     *         the class of the KrailView for this page. This can be null if a redirection will prevent it from
     *         actually
     *         being displayed, but it is up to the developer to ensure that the redirection is in place
     * @param labelKey
     *         the I18NKey for a localised label for the view
     * @param pageAccessControl
     *         the type of page access control to use
     * @param roles
     *         the comma separated list of roles which may access this page, may be null. Is ignored if {@code pageAccessControl} is not {@link
     *         PageAccessControl#ROLES}
     */
    protected void addEntry(String uri, Class<? extends KrailView> viewClass, I18NKey labelKey, PageAccessControl pageAccessControl, String roles) {

        DirectSitemapEntry entry = new DirectSitemapEntry(viewClass, labelKey, pageAccessControl, roles, 1);
        sitemapBinder.addBinding(uri)
                     .toInstance(entry);

    }

    /**
     * Adds an entry to be place in the {@link MasterSitemap} by the {@link DirectSitemapLoader}. Defaults the roles to null
     *
     * @param uri
     *         the URI for this page
     * @param viewClass
     *         the class of the KrailView for this page. This can be null if a redirection will prevent it from
     *         actually
     *         being displayed, but it is up to the developer to ensure that the redirection is in place
     * @param labelKey
     *         the I18NKey for a localised label for the view
     * @param pageAccessControl
     *         the type of page access control to use
     * @param positionIndex
     *         the position of a page in relation to its siblings.  Used as a sort order, relative numbering does not need to be sequential. A positionIndex
     *         < 0 indicates that the page should not be displayed in a navigation component
     */
    protected void addEntry(String uri, Class<? extends KrailView> viewClass, I18NKey labelKey, PageAccessControl pageAccessControl, int positionIndex) {

        DirectSitemapEntry entry = new DirectSitemapEntry(viewClass, labelKey, pageAccessControl, null, positionIndex);
        sitemapBinder.addBinding(uri)
                     .toInstance(entry);

    }

    /**
     * Adds an entry to be place in the {@link MasterSitemap} by the {@link DirectSitemapLoader}.
     *
     * @param uri
     *         the URI for this page
     * @param viewClass
     *         the class of the KrailView for this page. This can be null if a redirection will prevent it from
     *         actually
     *         being displayed, but it is up to the developer to ensure that the redirection is in place
     * @param labelKey
     *         the I18NKey for a localised label for the view
     * @param pageAccessControl
     *         the type of page access control to use
     * @param roles
     *         the comma separated list of roles which may access this page, may be null. Is ignored if {@code pageAccessControl} is not {@link
     *         PageAccessControl#ROLES}
     * @param positionIndex
     *         the position of a page in relation to its siblings.  Used as a sort order, relative numbering does not need to be sequential. A positionIndex
     *         < 0 indicates that the page should not be displayed in a navigation component
     */
    protected void addEntry(String uri, Class<? extends KrailView> viewClass, I18NKey labelKey, PageAccessControl pageAccessControl, String roles, int
            positionIndex) {

        DirectSitemapEntry entry = new DirectSitemapEntry(viewClass, labelKey, pageAccessControl, roles, positionIndex);
        sitemapBinder.addBinding(uri)
                     .toInstance(entry);

    }

    protected void addRedirect(String fromURI, String toURI) {
        redirectBinder.addBinding(fromURI)
                      .toInstance(new RedirectEntry(toURI));
    }
}