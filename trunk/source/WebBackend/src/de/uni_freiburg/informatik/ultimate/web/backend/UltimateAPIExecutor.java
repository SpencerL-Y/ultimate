package de.uni_freiburg.informatik.ultimate.web.backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.datalocation.Location;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.uni_freiburg.informatik.ultimate.core.coreplugin.Activator;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.PluginFactory;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.SettingsManager;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.ToolchainManager;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.UltimateCore.UltimateJobChangeAdapter;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.preferences.CorePreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.services.ToolchainStorage;
import de.uni_freiburg.informatik.ultimate.core.model.IUltimatePlugin;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.util.CoreUtil;

public class UltimateAPIExecutor implements IUltimatePlugin {
	private final ServletLogger mLogger;
	private static final long TIMEOUT = 24 * 60 * 60 * 1000;
	private File mInputFile;
	private File mToolchainFile;
	private File mSettingsFile;
	private UltimateBackendCore mUltimateBackendCore;
	public static final boolean DEBUG = !false;

	public UltimateAPIExecutor(final ServletLogger logger) {
		mLogger = logger;
	}

	/**
	 * Trigger an ultimate run based on an API request. Returns the results in the JSONobject.
	 * @param internalRequest
	 * @return Ultimate run results to be processed by the web-frontend.
	 * @throws JSONException
	 */
	public JSONObject executeUltimateRunRequest(final Request internalRequest) throws JSONException {
		mLogger.log("Start executing Ultimate for RequestId: " + internalRequest.getRequestId());
		JSONObject jsonResult = new JSONObject();
		
		// Prepare {input, toolchain, settings} as temporary files.
		try {
			final String timestamp = CoreUtil.getCurrentDateTimeAsString();
			setInputFile(internalRequest, timestamp);
			setToolchainFile(internalRequest, timestamp);
			setSettingsFile(internalRequest, timestamp);
			mLogger.log("Written temporary files to " + mInputFile.getParent() + " with timestamp " + timestamp);
		} catch (IOException e) {
			jsonResult = new JSONObject();
			jsonResult.put("error", "Internal server error: IO");
			mLogger.log("Internal server error: " + e.getClass().getSimpleName());
			mLogger.logDebug(e.toString());

			if (DEBUG) {				
				e.printStackTrace();
			}
			return jsonResult;
		}
		
		// Append user settings to the temporary settings file.
		try {
			applyUserSettings(internalRequest);
		} catch (IOException e) {
			mLogger.log("Could not apply user settings: " + e.getMessage());
		}
		
		// run ultimate
		// TODO: Allow timeout to be set in the API request.
		final long timeout = Math.min(TIMEOUT, TIMEOUT);
		if (runUltimateViaBackendController(jsonResult, timeout)) {
			mLogger.log("Finished executing Ultimate.");
		} else {
			mLogger.log("Ultimate terminated abnormally.");
		}

		return jsonResult;
	}

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
	 * Run a ultimate session via UltimateWebController. Add the results to the json object to be used as API response.
	 * @param json
	 * @param timeout
	 * @return
	 * @throws JSONException
	 */
	private boolean runUltimateViaBackendController(final JSONObject json, final long timeout) throws JSONException {
		mLogger.log("Init UltimateBackendCore");
		mUltimateBackendCore = new UltimateBackendCore();
		
		mLogger.log("Init UltimateBackendController");
		UltimateBackendController mUltimateBackendController = new UltimateBackendController();
		mUltimateBackendController.init(mUltimateBackendCore);
		
		return true;
	}

	/**
	 * Run a ultimate session via UltimateWebController. Add the results to the json object to be used as API response.
	 * @param json
	 * @param timeout
	 * @return
	 * @throws JSONException
	 */
	private boolean runUltimate(final JSONObject json, final long timeout) throws JSONException {
		try {
			mLogger.log("Starting Ultimate ...");
			final UltimateBackendController ubc = new UltimateBackendController(); 
			final UltimateWebController uwc =
					new UltimateWebController(mLogger, mSettingsFile, mInputFile, mToolchainFile, timeout);
			uwc.runUltimate(json);
		} catch (final Throwable t) {
			mLogger.log("Failed to run Ultimate.");
			json.put("error", "Failed to run ULTIMATE: " + t.getMessage());
			return false;
		} finally {
			postProcessTemporaryFiles();
		}
		return true;
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

	/**
	 * Appends the settings from the web-frontend to the ultimate settings file.
	 * @param internalRequest
	 * @throws IOException
	 */
	private void applyUserSettings(final Request internalRequest) throws IOException {
		try {
			mLogger.log("Apply user settings to run configuration.");
			final JSONObject jsonParameter = new JSONObject(internalRequest.getSingleParameter("user_settings"));
			final JSONArray userSettings = jsonParameter.getJSONArray("user_settings");
			BufferedWriter settingsOutput = new BufferedWriter(new FileWriter(mSettingsFile.getAbsolutePath(), true));
			
			settingsOutput.newLine();
			settingsOutput.write("# User settings from the webfrontend: ");
			for (int i=0; i < userSettings.length(); i++) {
			    final JSONObject userSetting = userSettings.getJSONObject(i);
			    switch (userSetting.getString("type")) {
				case "bool":
					settingsOutput.newLine();
					settingsOutput.write(userSetting.getString("string") + "=" + userSetting.getBoolean("checked"));
					break;
				default:
					mLogger.log("User setting type " + userSetting.getString("type") + " is unknown. Ignoring");
				}
			}
			settingsOutput.newLine();
			settingsOutput.close();			
		} catch (JSONException e) {
			mLogger.log("Could not fetch user settings: " + e.getMessage());
		}
		
	}

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
}
