package de.uni_freiburg.informatik.ultimate.web.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import de.uni_freiburg.informatik.ultimate.core.coreplugin.Activator;
import de.uni_freiburg.informatik.ultimate.core.lib.toolchain.RunDefinition;
import de.uni_freiburg.informatik.ultimate.core.model.IController;
import de.uni_freiburg.informatik.ultimate.core.model.ICore;
import de.uni_freiburg.informatik.ultimate.core.model.ISource;
import de.uni_freiburg.informatik.ultimate.core.model.ITool;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchainData;
import de.uni_freiburg.informatik.ultimate.core.model.IUltimatePlugin;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.model.results.IResult;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.util.CoreUtil;

public class UltimateAPIController implements IUltimatePlugin, IController<RunDefinition> {
	
	private final ServletLogger mLogger;
	private static final long TIMEOUT = 15 * 1000;
	private File mInputFile;
	private File mToolchainFile;
	private File mSettingsFile;
	private Request mRequest;
	private JSONObject mResult;
	private ICore<RunDefinition> mCore;
	public static final boolean DEBUG = !false;

	public UltimateAPIController(final Request request, JSONObject result) {
		mLogger = request.getLogger();
		mRequest = request;
		mResult = result;
	}
	
	public void run() {
		// TODO: Allow timeout to be set in the API request and use it.
		final long timeout = Math.min(TIMEOUT, TIMEOUT);
		try {
			WebBackendToolchainJob job = new WebBackendToolchainJob(
					"WebBackendToolchainJob for request " + mRequest.getRequestId(), 
					mCore, this, mLogger, new File[] { mInputFile }, mResult, mRequest);
			job.schedule();
			job.join();
		} catch (final Throwable t) {
			mLogger.log("Failed to run Ultimate.");
			try {
				mResult.put("error", "Failed to run ULTIMATE: " + t.getMessage());
			} catch (JSONException e) {
				if (DEBUG) {
					e.printStackTrace();
				}
			}
		} finally {
			postProcessTemporaryFiles();
		}
	}
	

	/**
	 * Add user frontend settings to the plugins in the toolchain.
	 * @param tcData
	 * @return Updated UltimateServiceProvider
	 */
	private IUltimateServiceProvider addUserSettings(IToolchainData<RunDefinition> tcData) {
		IUltimateServiceProvider services = tcData.getServices();
		
		/* 
		// Debug: traverse the toolchain to log its content.
		RunDefinition tcRD = tcData.getRootElement();
		ToolchainListType tc = tcRD.getToolchain();
		List<Object> tcPluginOrSubchain = tc.getPluginOrSubchain();
		for (Object pluginOrSubchain : tcPluginOrSubchain) {
			mLogger.log(pluginOrSubchain.toString());
		}
		*/
		
		
		// Get the user settings from the request
		try {
			mLogger.log("Apply user settings to run configuration.");
			final JSONObject jsonParameter = new JSONObject(mRequest.getSingleParameter("user_settings"));
			final JSONArray userSettings = jsonParameter.getJSONArray("user_settings");

			for (int i=0; i < userSettings.length(); i++) {
			    final JSONObject userSetting = userSettings.getJSONObject(i);
			    String pluginId = userSetting.getString("plugin_id");
			    String key = userSetting.getString("key");
			    
			    // Check if the setting is in the white-list.
				if (Config.USER_SETTINGS_WHITELIST.PluginKeyIsWhitelisted(pluginId, key) == false) {
					mLogger.log("User setting for plugin=" + pluginId + " key=" + key + " is not in whitelist. Ignoring.");
					continue;
				}
			    
				// Apply the setting.
				switch (userSetting.getString("type")) {
				case "bool":
					services.getPreferenceProvider(pluginId).put(
							key, userSetting.getBoolean("checked"));
					break;
				default:
					mLogger.log("User setting type " + userSetting.getString("type") + " is unknown. Ignoring");
				}
			}
		} catch (JSONException e) {
			mLogger.log("Could not fetch user settings: " + e.getMessage());
		}
				
		return services;
	}

	/******************* Ultimate Plugin Implementation *****************/
	
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
	
	/**************** End Ultimate Plugin Implementation *****************/
	
	/**************** IController Implementation *****************/

	@Override
	public int init(ICore<RunDefinition> core) {
		if (core == null) {
			return -1;
		}
		
		mCore = core;
		
		// Prepare {input, toolchain, settings} as temporary files.
		mLogger.log("Prepare input files for RequestId: " + mRequest.getRequestId());
		try {
			final String timestamp = CoreUtil.getCurrentDateTimeAsString();
			setInputFile(mRequest, timestamp);
			setToolchainFile(mRequest, timestamp);
			// TODO: we do not longer use the settings file.
			setSettingsFile(mRequest, timestamp);
			mLogger.log("Written temporary files to " + mInputFile.getParent() + " with timestamp " + timestamp);
		} catch (IOException e) {
			try {
				mResult.put("error", "Internal server error: IO");
			} catch (JSONException eJson) {
				if(DEBUG) {					
					eJson.printStackTrace();
				}
			}
			mLogger.log("Internal server error: " + e.getClass().getSimpleName());
			mLogger.logDebug(e.toString());

			if (DEBUG) {				
				e.printStackTrace();
			}
			return -1;
		}
		
		core.resetPreferences(false);
		
		return 0;
	}

	@Override
	public ISource selectParser(Collection<ISource> parser) {
		return null;
	}

	@Override
	public IToolchainData<RunDefinition> selectTools(List<ITool> tools) {
		try {
			final IToolchainData<RunDefinition> tc = mCore.createToolchainData(mToolchainFile.getAbsolutePath());
			return tc;
		} catch (FileNotFoundException | JAXBException | SAXException e) {
			mLogger.error("Exception during tool selection: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;
		}
	}

	@Override
	public List<String> selectModel(List<String> modelNames) {
		return null;
	}

	@Override
	public IToolchainData<RunDefinition> prerun(IToolchainData<RunDefinition> tcData) {
		return tcData.replaceServices(addUserSettings(tcData));
	}

	@Override
	public void displayToolchainResults(IToolchainData<RunDefinition> toolchain, Map<String, List<IResult>> results) {
		
	}

	@Override
	public void displayException(IToolchainData<RunDefinition> toolchain, String description, Throwable ex) {
		
	}
	
	/**************** End IController Implementation *****************/
	
	/**
	 * Set the temporary ultimate input file. As set by the web-frontend user in the editor.
	 * @param internalRequest
	 * @param timestamp
	 * @throws IOException
	 */
	private void setInputFile(Request internalRequest, String timestamp) throws IOException {
		final String code = internalRequest.getSingleParameter("code");
		final String fileExtension = internalRequest.getSingleParameter("code_file_extension");
		mInputFile = writeTemporaryFile(timestamp + "_input", code, fileExtension);
	}

	/**
	 * Set temporary settings file as sent by the web-frontend.
	 * @param internalRequest
	 * @param timestamp
	 * @throws IOException
	 */
	private void setToolchainFile(Request internalRequest, String timestamp) throws IOException {
		final String ultimate_toolchain_xml = internalRequest.getSingleParameter("ultimate_toolchain_xml");
		mToolchainFile = writeTemporaryFile(timestamp + "_toolchain", ultimate_toolchain_xml, ".xml");
	}
	
	/**
	 * Create temporary settings file as sent by the web-frontend.
	 * @param internalRequest
	 * @param timestamp
	 * @throws IOException
	 */
	private void setSettingsFile(Request internalRequest, String timestamp) throws IOException {
		final String ultimate_settings_epf = internalRequest.getSingleParameter("ultimate_settings_epf");
		mSettingsFile = writeTemporaryFile(timestamp + "_settings", ultimate_settings_epf, ".epf");
	}

	/**
	 * Move the temporary files to the "log dir" (log folder in the temp dir).
	 */
	private void postProcessTemporaryFiles() {
		final File logDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "log" + File.separator);
		if (!logDir.exists()) {
			logDir.mkdir();
		}
		mLogger.log("Moving input, setting and toolchain file to " + logDir.getAbsoluteFile());
		if (mInputFile != null) {
			mInputFile.renameTo(new File(logDir, mInputFile.getName()));
		}
		if (mSettingsFile != null) {
			mSettingsFile.renameTo(new File(logDir, mSettingsFile.getName()));
		}
		if (mToolchainFile != null) {
			mToolchainFile.renameTo(new File(logDir, mToolchainFile.getName()));
		}
	}

	/**
	 * Creates a file in the default temporary-file.
	 * @param name The name of the file (without file extension).
	 * @param content Content to end up in the file.
	 * @param fileExtension File extension to be used in the file path.
	 * @return
	 * @throws IOException
	 */
	private static File writeTemporaryFile(final String name, final String content, final String fileExtension)
			throws IOException {
		final File codeFile = File.createTempFile(name, fileExtension);
		try (final Writer fstream = new OutputStreamWriter(new FileOutputStream(codeFile), StandardCharsets.UTF_8)) {
			fstream.write(content);
		}
		return codeFile;
	}
}
