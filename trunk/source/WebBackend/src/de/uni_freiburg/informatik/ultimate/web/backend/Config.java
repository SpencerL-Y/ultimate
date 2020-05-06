package de.uni_freiburg.informatik.ultimate.web.backend;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * WebBackend settings.
 * 
 * # Available settings:
 * Config.DEBUG	.......... // (bool) If true be more verbose.
 * Config.SERVE_WEBSITE .. // (bool) If true, the static front-end will be served at http://host:Config.PORT/Config.FRONTEND_ROUTE
 * Config.PORT ........... // (int) Port Jetty will be serving at.
 * Config.FRONTEND_PATH .. // (string) absolute path to the front-end root directory (/trunk/source/WebsiteStatic) in ultimate repo.
 * Config.FRONTEND_ROUTE . // (string) The URL slug the front-end will be served at (e.g. http://host:Config.PORT/website).
 * Config.BACKEND_ROUTE .. // (string) The URL slug the back-end will be served at (e.g. http://host:Config.PORT/api).
 *  
 * # How to change settings.
 * 	1. Uses default setting constants as defined here.
 * 	2. Overrides settings provided by a "web.config.properties" file.
 * 	3. Overrides settings provided by VM arguments e.g.:
 * 		--DWebBackend.DEBUG=false
 * 		--DWebBackend.PORT=8080
 * 		--DWebBackend.SERVE_WEBSITE=true
 * 		--DWebBackend.FRONTEND_PATH="path/to/trunk/source/WebsiteStatic"
 * 		--DWebBackend.FRONTEND_ROUTE="/website"
 * 		--DWebBackend.BACKEND_ROUTE="/api"
 * 		--DWebBackend.SETTINGS_WHITELIST="/path/to/your/settings_whitelist.json"
 */
public class Config {
	public static boolean DEBUG = true;
	public static boolean SERVE_WEBSITE = true;
	public static int PORT = 8080;
	public static String FRONTEND_PATH = "website_static";
	public static String FRONTEND_ROUTE = "/website";
	public static String BACKEND_ROUTE = "/api";
	public static String SETTINGS_WHITELIST = "settings_whitelist.json";
	public static UserSettingsWhitelist USER_SETTINGS_WHITELIST;
	
	private static Properties appSettings = new Properties();
	private final static String settingsFile = "web.config.properties";
	private final static String propertyPrefix = "WebBackend.";

	/**
	 * Load settings from web.config.properties file
	 */
	public static void load() {
		loadSettingsFile();
		loadSettings();
	}
	
	/**
	 * Load settings file into Properties object.
	 */
	private static void loadSettingsFile() {
		try {
			final FileInputStream fileInputStream = new FileInputStream(settingsFile);
			appSettings.load(fileInputStream);
			fileInputStream.close();

			if (DEBUG) {
				System.out.println("web.config.properties settings file successfuly loaded.");
			}
		} catch (final IOException e) {
			System.out.println("Could not load web.config.properties settings file. Skipping.");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Load available settings. Overrides the defaults by the results if any.
	 */
	private static void loadSettings() {
		DEBUG = loadBoolean("DEBUG", DEBUG);
		SERVE_WEBSITE = loadBoolean("SERVE_WEBSITE", SERVE_WEBSITE);
		PORT = loadInteger("PORT", PORT);
		FRONTEND_PATH = loadString("FRONTEND_PATH", FRONTEND_PATH);
		FRONTEND_ROUTE = loadString("FRONTEND_ROUTE", FRONTEND_ROUTE);
		BACKEND_ROUTE = loadString("BACKEND_ROUTE", BACKEND_ROUTE);
		USER_SETTINGS_WHITELIST = new UserSettingsWhitelist(loadString("SETTINGS_WHITELIST", SETTINGS_WHITELIST));
	}


	/**
	 * Load the setting string named `propertyName`. 
	 * Returns `defaultValue` if setting is not found. 
	 * Prefers vmArguments before settings file.
	 * 
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	private static String loadString(String propertyName, String defaultValue) {
		String result = defaultValue;
		if (appSettings.get(propertyName) != null) {
			result = (String) appSettings.get(propertyName);
		}
		if (System.getProperty("WebBackend." + propertyName) != null) {
			result = System.getProperty(propertyPrefix  + propertyName);
		}
		
		return result;
	}

	/**
	 * Load the setting boolean named `propertyName`. 
	 * Returns `defaultValue` if setting is not found. 
	 * Prefers vmArguments before settings file.
	 * 
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	private static Boolean loadBoolean(String propertyName, boolean defaultValue) {
		boolean result = defaultValue;
		if (appSettings.get(propertyName) != null) {			
			result = Boolean.parseBoolean((String) appSettings.get(propertyName));
		}
		if (System.getProperty("WebBackend." + propertyName) != null) {
			result = Boolean.parseBoolean(System.getProperty("WebBackend." + propertyName));
		}
		
		return result;
	}
	
	/**
	 * Load the setting integer named `propertyName`. 
	 * Returns `defaultValue` if setting is not found. 
	 * Prefers vmArguments before settings file.
	 * 
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	private static Integer loadInteger(String propertyName, Integer defaultValue) {
		Integer result = defaultValue;
		if (appSettings.get(propertyName) != null) {			
			result = Integer.parseInt((String) appSettings.get(propertyName));
		}
		if (System.getProperty("WebBackend." + propertyName) != null) {
			result = Integer.parseInt(System.getProperty("WebBackend." + propertyName));
		}
		
		return result;
	}
}
