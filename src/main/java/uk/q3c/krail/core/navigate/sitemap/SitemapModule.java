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

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;

import uk.q3c.krail.core.navigate.sitemap.DefaultSitemap.RedirectNode;
import uk.q3c.krail.core.navigate.sitemap.DefaultSitemap.ViewNode;
import uk.q3c.krail.core.navigate.sitemap.annotations.AnnotationSitemapLoader;
import uk.q3c.krail.core.navigate.sitemap.impl.AbstractNode;
import uk.q3c.krail.core.navigate.sitemap.impl.ParametersImpl;
import uk.q3c.krail.core.view.DefaultLoginView;
import uk.q3c.krail.core.view.DefaultLogoutView;
import uk.q3c.krail.core.view.DefaultPrivateHomeView;
import uk.q3c.krail.core.view.DefaultPublicHomeView;
import uk.q3c.krail.core.view.ErrorView;
import uk.q3c.util.TableBuilder;

public class SitemapModule extends AbstractModule {

	private static final Logger LOGGER = LoggerFactory.getLogger(SitemapModule.class);

	private static class SitemapProvider implements Provider<Sitemap> {

		private final Sitemap sitemap;

		@Inject
		public SitemapProvider(Set<SitemapLoader> loaders, DefaultSitemap sitemap) {
			this.sitemap = sitemap;

			for (SitemapLoader loader : loaders) {
				loader.configure(sitemap);
			}

			sitemap.addView("error", ErrorView.class).setAccesControlRule(AccesControl.PUBLIC);

			setMissingStandardViews(sitemap);

			report(sitemap);
		}

		private void report(DefaultSitemap sitemap) {
			TreeMap<String, AbstractNode> nodes = new TreeMap<String, AbstractNode>();

			for (AbstractNode node : sitemap.getNodes()) {
				nodes.put(node.getUriPattern(), node);
			}

			StringBuilder sb = new StringBuilder();
			sb.append("-----------------------------------------" + "\n");
			sb.append("Sitemap Report" + "\n");
			sb.append("-----------------------------------------" + "\n");
			sb.append("Views and Redirects:" + "\n");
			{
				TableBuilder tabeBuilder = new TableBuilder();
				for (Entry<String, AbstractNode> node : nodes.entrySet()) {
					tabeBuilder.addRow("   ", "/" + node.getKey(), "  to  ", format(node.getValue()), "  in  ",
							node.getValue().getAccesControlRule().toString());
				}
				sb.append(tabeBuilder.toString() + "\n");
			}

			sb.append("Standard Pages:" + "\n");
			{
				TableBuilder tabeBuilder = new TableBuilder();
				for (Entry<StandardPageKey, SitemapNode> sp : sitemap.getStandardViews().entrySet()) {
					tabeBuilder.addRow("   ", sp.getKey().toString(), "  to  ", format(sp.getValue()));
				}
				sb.append(tabeBuilder.toString() + "\n");
			}
			sb.append("-----------------------------------------" + "\n");
			LOGGER.debug("{}", sb);
		}

		private String format(SitemapNode node) {
			if (node instanceof RedirectNode) {
				SitemapNode target = ((RedirectNode) node).getTargetNode();
				return "redirect(" + format(target) + ")";
			} else if (node instanceof ViewNode) {
				return "view(" + node.getViewClass().getName() + ")";
			} else {
				LOGGER.warn("Unexpected node type: {}", node.getClass());
				return node.toString();
			}
		}

		@Override
		public Sitemap get() {
			return sitemap;
		}

		private void setMissingStandardViews(Sitemap sitemap) {
			if (sitemap.getStandardView(StandardPageKey.Public_Home) == null) {
				ViewNode node = sitemap.addView("", DefaultPublicHomeView.class);
				node.setAccesControlRule(AccesControl.PUBLIC);
				sitemap.setStandardView(StandardPageKey.Public_Home, node);
			}
			if (sitemap.getStandardView(StandardPageKey.Private_Home) == null) {
				ViewNode node = sitemap.addView("private", DefaultPrivateHomeView.class);
				node.setAccesControlRule(AccesControl.AUTHENTICATED);
				sitemap.setStandardView(StandardPageKey.Private_Home, node);
			}
			if (sitemap.getStandardView(StandardPageKey.Log_In) == null) {
				ViewNode node = sitemap.addView("login", DefaultLoginView.class);
				node.setAccesControlRule(AccesControl.PUBLIC);
				sitemap.setStandardView(StandardPageKey.Log_In, node);
			}
			if (sitemap.getStandardView(StandardPageKey.Log_Out) == null) {
				ViewNode node = sitemap.addView("logout", DefaultLogoutView.class);
				node.setAccesControlRule(AccesControl.PUBLIC);
				sitemap.setStandardView(StandardPageKey.Log_Out, node);
			}
		}
	}

	private final Reflections basePackageReflections;

	public SitemapModule(String basePackage) {
		this(new Reflections(basePackage));
	}

	public SitemapModule(Reflections basePackageReflections) {
		super();
		this.basePackageReflections = basePackageReflections;
	}

	@Override
	protected void configure() {
		Multibinder<SitemapLoader> sitemapLoadersBinder = Multibinder.newSetBinder(binder(), SitemapLoader.class);

		bindLoaders(sitemapLoadersBinder);

		bind(Sitemap.class).toProvider(SitemapProvider.class).asEagerSingleton();
		// FIXME: static injection
		requestStaticInjection(ParametersImpl.class);

	}

	/**
	 * Overide this to chaange the default used SitemapLoader
	 * (AnnotationSitemapLoader that will scann all the classpath) or add others:
	 * <br>
	 * <br>
	 * <code>
	 * 	super.bindLoaders(sitemapLoadersBinder); //to keep the Default Loader
	 * 	sitemapLoadersBinder.addBinding().to(MyLoader.class);
	 * </code>
	 */
	protected void bindLoaders(Multibinder<SitemapLoader> sitemapLoadersBinder) {
		sitemapLoadersBinder.addBinding().toInstance(new AnnotationSitemapLoader(basePackageReflections));
	}

}
