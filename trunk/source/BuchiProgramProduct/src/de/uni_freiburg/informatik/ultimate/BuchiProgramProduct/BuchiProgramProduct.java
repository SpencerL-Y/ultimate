package de.uni_freiburg.informatik.ultimate.BuchiProgramProduct;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.access.IObserver;
import de.uni_freiburg.informatik.ultimate.core.api.UltimateServices;
import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.IGenerator;
import de.uni_freiburg.informatik.ultimate.model.GraphType;
import de.uni_freiburg.informatik.ultimate.model.IElement;

/**
 * This plugin implements the product algorithm described in the Masterthesis
 * "Automatische Generierungvon Buchi-Programmen".
 * 
 * 
 * @author Langenfeld
 * 
 * 
 */
public class BuchiProgramProduct implements IGenerator {

	protected static Logger mLogger;
	protected List<String> mFileNames;

	private BuchiProductObserver mBuchiProductObserver;
	private boolean mProcess;

	@Override
	public GraphType getOutputDefinition() {
		List<String> filenames = new ArrayList<String>();
		filenames.add("Product");

		return new GraphType(Activator.PLUGIN_ID, GraphType.Type.OTHER, filenames);
	}

	@Override
	public boolean isGuiRequired() {
		return false;
	}

	@Override
	public QueryKeyword getQueryKeyword() {
		return QueryKeyword.ALL;
	}

	@Override
	public void setInputDefinition(GraphType graphType) {
		switch (graphType.getCreator()) {
		case "LTL2aut":
		case "RCFGBuilder":
			mProcess = true;
			break;
		default:
			mProcess = false;
			break;
		}
	}

	@Override
	public List<IObserver> getObservers() {
		ArrayList<IObserver> observers = new ArrayList<IObserver>();
		if (mProcess) {
			observers.add(mBuchiProductObserver);
		}
		return observers;
	}

	@Override
	public int init() {
		mBuchiProductObserver = new BuchiProductObserver();
		mProcess = false;
		mFileNames = new ArrayList<String>();
		mLogger = UltimateServices.getInstance().getLogger(Activator.PLUGIN_ID);
		return 0;
	}

	@Override
	public String getName() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public String getPluginID() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public IElement getModel() {
		if (mBuchiProductObserver.getProduct() != null) {
			return mBuchiProductObserver.getProduct().getProductRCFG();
		} else {
			return null;
		}
	}

	@Override
	public List<String> getDesiredToolID() {
		return null;
	}

	@Override
	public UltimatePreferenceInitializer getPreferences() {
		return null;
	}

}
