package de.uni_freiburg.informatik.ultimate.web.backend;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.json.JSONException;
import org.json.JSONObject;

import de.uni_freiburg.informatik.ultimate.core.coreplugin.Activator;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.RcpProgressMonitorWrapper;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.exceptions.ParserInitializationException;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.toolchain.DefaultToolchainJob;
import de.uni_freiburg.informatik.ultimate.core.lib.toolchain.RunDefinition;
import de.uni_freiburg.informatik.ultimate.core.model.IController;
import de.uni_freiburg.informatik.ultimate.core.model.ICore;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchainData;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchainProgressMonitor;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchain.ReturnCode;

public class WebBackendToolchainJob extends DefaultToolchainJob {

	private JSONObject mResult;
	private ServletLogger mLogger;
	private Request mRequest;

	public WebBackendToolchainJob(String name, ICore<RunDefinition> core, IController<RunDefinition> controller,
			ServletLogger logger, File[] input, JSONObject result, Request request) {
		super(name, core, controller, logger, input);
		mResult = result;
		mLogger = logger;
		mRequest = request;
	}
	
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final IToolchainProgressMonitor tpm = RcpProgressMonitorWrapper.create(monitor);
		tpm.beginTask(getName(), IProgressMonitor.UNKNOWN);

		try {
			setToolchain(mCore.requestToolchain(mInputFiles));
			tpm.worked(1);

			mToolchain.init(tpm);
			tpm.worked(1);

			if (!mToolchain.initializeParsers()) {
				throw new ParserInitializationException();
			}
			tpm.worked(1);

			final IToolchainData<RunDefinition> chain = mToolchain.makeToolSelection(tpm);
			if (chain == null) {
				mLogger.fatal("Toolchain selection failed, aborting...");
				return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, IStatus.CANCEL, "Toolchain selection canceled",
						null);
			}
			setServices(chain.getServices());
			tpm.worked(1);

			mToolchain.runParsers();
			tpm.worked(1);

			return convert(mToolchain.processToolchain(tpm));
		} catch (final Throwable e) {
			return handleException(e);
		} finally {
			tpm.done();
			releaseToolchain();
		}
	}
	
	@Override
	protected IStatus convert(final ReturnCode result) {
		switch (result) {
		case Ok:
		case Cancel:
			try {
				UltimateResultProcessor.processUltimateResults(
						mLogger, mToolchain.getCurrentToolchainData().getServices(), mResult);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		case Error:
		default:
			return super.convert(result);
		}
	}

}
