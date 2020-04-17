package de.uni_freiburg.informatik.ultimate.web.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import de.uni_freiburg.informatik.ultimate.core.coreplugin.Activator;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.PluginFactory;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.SettingsManager;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.ToolchainManager;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.services.ToolchainStorage;
import de.uni_freiburg.informatik.ultimate.core.lib.toolchain.RunDefinition;
import de.uni_freiburg.informatik.ultimate.core.lib.toolchain.ToolchainData;
import de.uni_freiburg.informatik.ultimate.core.model.ICore;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchain;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchainData;
import de.uni_freiburg.informatik.ultimate.core.model.IUltimatePlugin;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceProvider;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILoggingService;


public class UltimateAPIServlet extends HttpServlet implements ICore<RunDefinition>, IUltimatePlugin {
	
	private static final long serialVersionUID = 1L;
	private static final boolean DEBUG = !false;
	private final ServletLogger mLogger;
	private ToolchainManager mToolchainManager;
	private ToolchainStorage mCoreStorage;
	private SettingsManager mSettingsManager;
	private PluginFactory mPluginFactory;
	private ILoggingService mLoggingService;
	
	/**
	 * Constructor.
	 *
	 * @see HttpServlet#HttpServlet()
	 */
	public UltimateAPIServlet() {
		super();
		mLogger = new ServletLogger(this, "Servlet", DEBUG);
		mCoreStorage = new ToolchainStorage();
		mLoggingService = mCoreStorage.getLoggingService();
		mSettingsManager = new SettingsManager(mLogger);
		mSettingsManager.registerPlugin(this);
		mPluginFactory = new PluginFactory(mSettingsManager, mLogger);
	}
	
	/**
	 * Process GET requests
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		mLogger.logDebug("Connection from " + request.getRemoteAddr() + ", GET: " + request.getQueryString());
	}
	
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		mLogger.logDebug("Connection from " + request.getRemoteAddr() + ", POST: " + request.getRequestURI());
		mLogger.logDebug("Init session logger");
		final ServletLogger sessionLogger = new ServletLogger(this, request.getSession().getId(), DEBUG);
		mLogger.logDebug("Init internal request.");
		final Request internalRequest = new Request(request, sessionLogger);
		
		// Set response type to JSON.
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		final PrintWriter responseWriter = response.getWriter();
		mLogger.logDebug("Process API request.");
		processAPIRequest(internalRequest, responseWriter);
	}

	/**
	 * Pass the Request to initiateUltimateRun.
	 * Write results to responseWriter.
	 * 
	 * @param internalRequest
	 * @param responseWriter
	 */
	private void processAPIRequest(Request internalRequest, PrintWriter responseWriter) {
		try {
			JSONObject jsonResult = new JSONObject();
			if (internalRequest.getParameterList().containsKey("action")) {
				mLogger.logDebug("Initiate ultimate run for request: " + internalRequest.toString());
				jsonResult = initiateUltimateRun(internalRequest);
			} else {
				jsonResult.put("error", "Invalid request: Missing `action` parameter.");
			}

			jsonResult.write(responseWriter);

		} catch (final JSONException e) {
			final String message = "{\"error\" : \"Invalid request: " + e.getMessage() + " \"}";
			responseWriter.print(message);
			internalRequest.getLogger().logDebug(message);
			
			if (DEBUG) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initiate ultimate run for the request.
	 * Return the results as a json object.
	 * 
	 * @param internalRequest
	 * @return
	 * @throws JSONException
	 */
	private JSONObject initiateUltimateRun(Request internalRequest) throws JSONException {
		try {
			final String action = internalRequest.getSingleParameter("action");
			if (action.equals("execute")) {
				final JSONObject json = new JSONObject();
				final UltimateAPIController controller = new UltimateAPIController(internalRequest, json);				
				int status = controller.init(this);
				mToolchainManager = new ToolchainManager(mLoggingService, mPluginFactory, controller);
				if (status == 0) {
					controller.run();
				}
				mToolchainManager.close();

				return json;
			} else {
				internalRequest.getLogger().logDebug("Don't know how to handle action: " + action);
				final JSONObject json = new JSONObject();
				json.put("error", "Invalid request: Unknown `action` parameter ( " + action + ").");
				return json;
			}
		} catch (IllegalArgumentException e) {
			final JSONObject json = new JSONObject();
			json.put("error", "Invalid request: " + e.getMessage());
			return json;
		}
	}

	/***************************** ICore Implementation *********************/
	
	@Override
	public IToolchainData<RunDefinition> createToolchainData(String filename)
			throws FileNotFoundException, JAXBException, SAXException {
		if (!new File(filename).exists()) {
			throw new FileNotFoundException("The specified toolchain file " + filename + " was not found");
		}

		final ToolchainStorage tcStorage = new ToolchainStorage();
		return new ToolchainData(filename, tcStorage, tcStorage);
	}

	@Override
	public IToolchainData<RunDefinition> createToolchainData() {
		return null;
	}

	@Override
	public IToolchain<RunDefinition> requestToolchain(File[] inputFiles) {
		return mToolchainManager.requestToolchain(inputFiles);
	}

	@Override
	public void releaseToolchain(IToolchain<RunDefinition> toolchain) {
		mToolchainManager.releaseToolchain(toolchain);
	}

	@Override
	public void savePreferences(String absolutePath) {
		
	}

	@Override
	public void loadPreferences(String absolutePath, boolean silent) {
		
	}

	@Override
	public void resetPreferences(boolean silent) {
		
	}

	@Override
	public IUltimatePlugin[] getRegisteredUltimatePlugins() {
		return null;
	}

	@Override
	public String[] getRegisteredUltimatePluginIDs() {
		return null;
	}

	@Override
	public ILoggingService getCoreLoggingService() {
		return null;
	}

	@Override
	public IPreferenceProvider getPreferenceProvider(String pluginId) {
		return null;
	}

	@Override
	public String getUltimateVersionString() {
		return null;
	}
	
	/************************* End ICore Implementation *********************/
	
	/************************* IUltimatePlugin Implementation *********************/

	@Override
	public String getPluginName() {
		return Activator.PLUGIN_NAME;
	}

	@Override
	public String getPluginID() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public IPreferenceInitializer getPreferences() {
		return null;
	}
	
	/************************* End IUltimatePlugin Implementation *********************/
	
}
