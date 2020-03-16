package de.uni_freiburg.informatik.ultimate.web.backend;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import de.uni_freiburg.informatik.ultimate.core.lib.toolchain.RunDefinition;
import de.uni_freiburg.informatik.ultimate.core.model.ICore;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchain;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchainData;
import de.uni_freiburg.informatik.ultimate.core.model.IUltimatePlugin;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceProvider;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILoggingService;

public class UltimateBackendCore implements ICore<RunDefinition> {

	@Override
	public IToolchainData<RunDefinition> createToolchainData(String filename)
			throws FileNotFoundException, JAXBException, SAXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IToolchainData<RunDefinition> createToolchainData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IToolchain<RunDefinition> requestToolchain(File[] inputFiles) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void releaseToolchain(IToolchain<RunDefinition> toolchain) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void savePreferences(String absolutePath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadPreferences(String absolutePath, boolean silent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetPreferences(boolean silent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IUltimatePlugin[] getRegisteredUltimatePlugins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getRegisteredUltimatePluginIDs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILoggingService getCoreLoggingService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPreferenceProvider getPreferenceProvider(String pluginId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUltimateVersionString() {
		// TODO Auto-generated method stub
		return null;
	}

}
