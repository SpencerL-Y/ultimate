package de.uni_freiburg.informatik.ultimate.web.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserSettingsWhitelist {
	
	private JSONObject mJSONWhitelist; 
	
	public UserSettingsWhitelist(String filePath) {
		this.initFromFile(filePath);
	}
	
	/**
	 * Check if "pluginId" is available in the whitelist.
	 * @param pluginId
	 * @return
	 */
	public boolean PluginIdIsCovered(String pluginId) {
		try {
			mJSONWhitelist.getJSONArray(pluginId);
		} catch (JSONException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Check if "pluginId" has white-listed "key".
	 * @param pluginId
	 * @param key
	 * @return
	 */
	public boolean PluginKeyIsWhitelisted(String pluginId, String key) {
		try {
			JSONArray plugin_keys = getPluginKeys(pluginId);
			for (int i=0; i < plugin_keys.length(); i++) {
				String plugin_key = (String) plugin_keys.get(i);
				if (plugin_key.equals(key)) {
					return true;
				}
			}
		} catch (JSONException e) {
			return false;
		}
		return false;
		
	}
	
	private JSONArray getPluginKeys(String pluginId) throws JSONException {
		return mJSONWhitelist.getJSONArray(pluginId);
	}

	private void initFromFile(String filePath) {
		try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
			String jsonString = lines.collect(Collectors.joining());
			mJSONWhitelist = new JSONObject(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
