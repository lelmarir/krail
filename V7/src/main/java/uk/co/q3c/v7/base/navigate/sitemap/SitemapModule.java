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
package uk.co.q3c.v7.base.navigate.sitemap;

import java.util.Set;

import uk.co.q3c.v7.base.navigate.sitemap.DefaultSitemap.ViewNode;
import uk.co.q3c.v7.base.navigate.sitemap.annotations.AnnotationSitemapLoader;
import uk.co.q3c.v7.base.view.DefaultErrorView;
import uk.co.q3c.v7.base.view.DefaultLoginView;
import uk.co.q3c.v7.base.view.DefaultLogoutView;
import uk.co.q3c.v7.base.view.DefaultPrivateHomeView;
import uk.co.q3c.v7.base.view.DefaultPublicHomeView;
import uk.co.q3c.v7.base.view.ErrorView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;

public class SitemapModule extends AbstractModule {

	private static class SitemapProvider implements Provider<Sitemap> {

		private final Sitemap sitemap;

		@Inject
		public SitemapProvider(Set<SitemapLoader> loaders) {
			this.sitemap = new DefaultSitemap();

			for (SitemapLoader loader : loaders) {
				loader.configure(sitemap);
			}
			
			sitemap.addView("error", ErrorView.class).setAccesControlRule(AccesControl.PUBLIC);
			
			setMissingStandardViews(sitemap);
		}

		@Override
		public Sitemap get() {
			return sitemap;
		}

		private void setMissingStandardViews(Sitemap sitemap) {
			if (sitemap.getStandardView(StandardViewKey.Public_Home) == null) {
				ViewNode node = sitemap.addView("", DefaultPublicHomeView.class);
				node.setAccesControlRule(AccesControl.PUBLIC);
				sitemap.setStandardView(StandardViewKey.Public_Home, node);
			}
			if (sitemap.getStandardView(StandardViewKey.PrivateHome) == null) {
				ViewNode node = sitemap.addView("private", DefaultPrivateHomeView.class);
				node.setAccesControlRule(AccesControl.AUTHENTICATED);
				sitemap.setStandardView(StandardViewKey.PrivateHome, node);
			}
			if (sitemap.getStandardView(StandardViewKey.Log_In) == null) {
				ViewNode node = sitemap.addView("login", DefaultLoginView.class);
				node.setAccesControlRule(AccesControl.PUBLIC);
				sitemap.setStandardView(StandardViewKey.Log_In, node);
			}
			if (sitemap.getStandardView(StandardViewKey.Log_Out) == null) {
				ViewNode node = sitemap.addView("logout", DefaultLogoutView.class);
				node.setAccesControlRule(AccesControl.PUBLIC);
				sitemap.setStandardView(StandardViewKey.Log_Out, node);
			}
		}
	}

	@Override
	protected void configure() {
		Multibinder<SitemapLoader> sitemapLoadersBinder = Multibinder
				.newSetBinder(binder(), SitemapLoader.class);

		bindLoaders(sitemapLoadersBinder);

		bind(Sitemap.class).toProvider(SitemapProvider.class)
				.asEagerSingleton();
		
		bindErrorView();
	}

	/**
	 * Override to provide a custom implementation of the ErrorView
	 */
	protected void bindErrorView() {
		bind(ErrorView.class).to(DefaultErrorView.class);
	}

	/**
	 * Overide this to chaange the default used SitemapLoader
	 * (AnnotationSitemapLoader that will scann all the classpath) or add others: <br>
	 * <br>
	 * <code>
	 * 	super.bindLoaders(sitemapLoadersBinder); //to keep the Default Loader
	 * 	sitemapLoadersBinder.addBinding().to(MyLoader.class);
	 * </code>
	 */
	protected void bindLoaders(Multibinder<SitemapLoader> sitemapLoadersBinder) {
		sitemapLoadersBinder.addBinding().toInstance(new AnnotationSitemapLoader(""));
	}

}
