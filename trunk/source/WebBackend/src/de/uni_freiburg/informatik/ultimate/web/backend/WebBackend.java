package de.uni_freiburg.informatik.ultimate.web.backend;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.resource.PathResource;

import de.uni_freiburg.informatik.ultimate.core.preferences.RcpPreferenceProvider;

public class WebBackend implements IApplication {

	private Server mJettyServer;

	public WebBackend() {

	}

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		Config.load();
		
		initJettyServer();

		mJettyServer.start();
		mJettyServer.join();

		return EXIT_OK;
	}

	@Override
	public void stop() {
		try {
			mJettyServer.stop();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize Jetty front- and back-end server.
	 */
	private void initJettyServer() {
		mJettyServer = new Server(Config.PORT);
		final ContextHandlerCollection contexts = new ContextHandlerCollection();
		mJettyServer.setHandler(contexts);

		// Serve the website (front-end) as static content.
		if (Config.SERVE_WEBSITE) {
			addStaticPathToContext(contexts, Paths.get(Config.FRONTEND_PATH), Config.FRONTEND_ROUTE);
		}

		// Serve the API.
		// Prepare Handler for API servlets.
		final ServletContextHandler servlets = new ServletContextHandler(
				contexts, "/", ServletContextHandler.SESSIONS
		);
		// Enable CORS to allow ultimate back-end/front-end running on a separate port and domain.
		enableCorsOnServletContextHandler(servlets);
		// Add the API servlet.
		servlets.addServlet(new ServletHolder(new UltimateAPIServlet()), Config.BACKEND_ROUTE);
	}

	/**
	 * Serve files in folderPath static at the routePath.
	 *
	 * @param contextCollection
	 * @param folderPath
	 *            Path to the static files to be served.
	 * @param routePath
	 *            The route the files should be served at (e.g. "/media").
	 */
	private static void addStaticPathToContext(final HandlerCollection contextCollection, final Path folderPath,
			final String routePath) {
		final ResourceHandler frontendResourceHandler = new ResourceHandler();
		frontendResourceHandler.setDirectoriesListed(true);

		final ContextHandler frontendContextHandler = new ContextHandler();
		frontendContextHandler.setContextPath(routePath);
		frontendContextHandler.setBaseResource(new PathResource(folderPath));
		frontendContextHandler.setHandler(frontendResourceHandler);

		contextCollection.addHandler(frontendContextHandler);
	}

	/**
	 * Add CORS headers to the servlets in the servlet handler. 
	 * Enables the servlets to be called from outside their served domain.
	 *
	 * @param servlets
	 *            ServletContextHandler
	 */
	private static void enableCorsOnServletContextHandler(final ServletContextHandler servlets) {
		final FilterHolder filterHolder = new FilterHolder(CrossOriginFilter.class);
		filterHolder.setInitParameter("allowedOrigins", "*");
		filterHolder.setInitParameter("allowedMethods", "GET, POST");
		servlets.addFilter(filterHolder, "/*", null);
	}

}
