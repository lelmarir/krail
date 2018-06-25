package uk.q3c.krail.core.navigate.sitemap.annotations;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.q3c.krail.core.navigate.sitemap.AccesControl;
import uk.q3c.krail.core.navigate.sitemap.Sitemap;
import uk.q3c.krail.core.navigate.sitemap.SitemapLoader;
import uk.q3c.krail.core.navigate.sitemap.SitemapNode;
import uk.q3c.krail.core.navigate.sitemap.StandardPageKey;
import uk.q3c.krail.core.navigate.sitemap.DefaultSitemap.ViewNode;
import uk.q3c.krail.core.view.KrailView;

public class AnnotationSitemapLoader implements SitemapLoader {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AnnotationSitemapLoader.class);

	private Reflections basePackageReflections;

	public AnnotationSitemapLoader(Reflections basePackageReflections) {
		this.basePackageReflections = basePackageReflections;
	}

	@Override
	public void configure(Sitemap sitemap) {

		LOGGER.info("scanning {} for View annotations", basePackageReflections.getConfiguration().getUrls());

		// find the View annotations
		Set<Class<?>> typesWithView = basePackageReflections
				.getTypesAnnotatedWith(View.class);
		LOGGER.info("{} KrailViews with View annotation found",
				typesWithView.size());

		checkRedirectAnnotations(basePackageReflections, typesWithView);

		// process the View annotations
		for (Class<?> clazz : typesWithView) {
			LOGGER.debug("processing annotated View {}", clazz.getName());
			if (!KrailView.class.isAssignableFrom(clazz)) {
				throw new IllegalStateException(
						"Classes annotated with View should be a KrailView");
			} else {
				@SuppressWarnings("unchecked")
				Class<KrailView> viewClass = (Class<KrailView>) clazz;

				View annotation = viewClass.getAnnotation(View.class);

				String uri = annotation.uri();
				if(uri.startsWith("/")) {
					uri = uri.substring(1);
					LOGGER.debug("url trailing slash removed: '{}' -> '{}'", "/"+uri, uri);
				}
				
				LOGGER.debug("Adding {} view to sitemap for {} url", viewClass,
						uri);
				ViewNode node = sitemap.addView(uri, viewClass);

				configurePageAccesControl(node, viewClass);

				configureRedirects(sitemap, node, viewClass);

				configureStandardPage(sitemap, node, viewClass);
			}
		}
	}

	private void configurePageAccesControl(ViewNode node, Class<?> clazz) {
		RequiresAuthentication requireAuthentication = getAnnotation(clazz,
				RequiresAuthentication.class);
		RequiresPermissions requiresPermissions = getAnnotation(clazz,
				RequiresPermissions.class);
		RequiresRoles requireRoles = getAnnotation(clazz, RequiresRoles.class);
		RequiresGuest requiresGuest = getAnnotation(clazz, RequiresGuest.class);
		RequiresUser requiresUser = getAnnotation(clazz, RequiresUser.class);

		checkOnlyOneNotNull(requireAuthentication, requiresPermissions,
				requireRoles, requiresGuest, requiresUser);

		if (requireAuthentication != null) {
			node.setAccesControlRule(AccesControl.AUTHENTICATED);
		} else if (requiresPermissions != null) {
			node.setAccesControlRule(new AccesControl.Permission(
					requiresPermissions.value(), requiresPermissions.logical()));
		} else if (requireRoles != null) {
			node.setAccesControlRule(new AccesControl.Roles(requireRoles
					.value(), requireRoles.logical()));
		} else if (requiresGuest != null) {
			node.setAccesControlRule(AccesControl.GUEST);
		} else if (requiresUser != null) {
			node.setAccesControlRule(AccesControl.USER);
		} else {
			node.setAccesControlRule(AccesControl.PUBLIC);
		}
		LOGGER.debug("Setting AccesControlRule for {} to {}", clazz,
				node.getAccesControlRule());
	}

	private <A extends Annotation> A getAnnotation(Class<?> clazz,
			Class<A> annotationType) {
		assert clazz != null;
		while (clazz != null) {
			A annotation = clazz.getAnnotation(annotationType);
			if (annotation != null) {
				return annotation;
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	private void checkOnlyOneNotNull(Object... objects) {
		boolean found = false;
		for (Object o : objects) {
			if (o != null) {
				if (found == true) {
					throw new IllegalStateException(
							"OnlyOne parameter should be not null");
				}
				found = true;
			}
		}
	}

	private void checkRedirectAnnotations(Reflections reflections,
			Set<Class<?>> typesWithView) {
		// find the Redirect annotations
		Set<Class<?>> typesWithRedirect = reflections
				.getTypesAnnotatedWith(Redirect.class);
		LOGGER.debug("{} KrailViews with Redirect annotation found",
				typesWithRedirect.size());

		typesWithRedirect.removeAll(typesWithView);
		if (!typesWithRedirect.isEmpty()) {
			StringBuilder sb = new StringBuilder(
					"The followings classes are annotated with Redirect, but not View:\n");
			for (Class<?> clazz : typesWithRedirect) {
				sb.append("\t" + clazz + "\n");
			}
			throw new RuntimeException(sb.toString());
		}
	}

	private void configureRedirects(Sitemap sitemap, SitemapNode node,
			Class<KrailView> viewClass) {
		Redirect redirect = viewClass.getAnnotation(Redirect.class);
		Redirects redirects = viewClass.getAnnotation(Redirects.class);

		if (redirect != null && redirects != null) {
			throw new IllegalStateException(
					"a class should be annotated with Redirect or Redirects annotation, not both");
		}

		if (redirect != null) {
			LOGGER.debug("Adding redirect for url {} to node {}",
					redirect.uri(), node);
			sitemap.addRedirect(redirect.uri(), node);
		} else if (redirects != null) {
			for (Redirect r : redirects.redirects()) {
				LOGGER.debug("Adding redirect for url {} to node {}", r.uri(),
						node);
				sitemap.addRedirect(r.uri(), node);
			}
		}
	}

	private void configureStandardPage(Sitemap sitemap, ViewNode node,
			Class<KrailView> viewClass) {
		StandardPage annotation = viewClass.getAnnotation(StandardPage.class);
		if (annotation != null) {
			StandardPageKey[] standardViews = annotation.value();
			for (StandardPageKey viewKey : standardViews) {
				if (sitemap.getStandardView(viewKey) != null) {
					throw new IllegalStateException("The standard view "
							+ viewKey + " has been already set to "
							+ sitemap.getStandardView(viewKey)
							+ " whyle trying to set to " + node);
				}

				LOGGER.debug("Setting standard view {} to {}", viewKey, node);
				sitemap.setStandardView(viewKey, node);
			}
		}
	}
}
