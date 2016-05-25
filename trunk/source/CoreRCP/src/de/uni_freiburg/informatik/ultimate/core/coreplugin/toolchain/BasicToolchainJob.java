/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Core.
 * 
 * The ULTIMATE Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Core. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Core, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Core grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.core.coreplugin.toolchain;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.uni_freiburg.informatik.ultimate.core.coreplugin.Activator;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.preferences.CorePreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.lib.toolchain.ToolchainListType;
import de.uni_freiburg.informatik.ultimate.core.model.IController;
import de.uni_freiburg.informatik.ultimate.core.model.ICore;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchain.ReturnCode;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchainData;
import de.uni_freiburg.informatik.ultimate.core.model.models.ILocation;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceProvider;
import de.uni_freiburg.informatik.ultimate.core.model.results.IResult;
import de.uni_freiburg.informatik.ultimate.core.model.results.IResultWithLocation;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;

public abstract class BasicToolchainJob extends Job {

	protected static enum ChainMode {
		/**
		 * Run fresh toolchain
		 */
		DEFAULT,

		/**
		 * Run new toolchain on old input files
		 */
		KEEP_INPUT,

		/**
		 * Run old toolchain on old input files
		 */
		RERUN,

		/**
		 * Run old toolchain on new input files
		 */
		@Deprecated KEEP_Toolchain,
	}

	protected ChainMode mJobMode;
	protected ICore<ToolchainListType> mCore;
	protected IController<ToolchainListType> mController;
	protected ILogger mLogger;
	protected IToolchainData<ToolchainListType> mChain;
	protected IUltimateServiceProvider mServices;
	private long mDeadline;

	public BasicToolchainJob(String name, ICore<ToolchainListType> core, IController<ToolchainListType> controller,
			ILogger logger) {
		super(name);
		assert logger != null;
		mCore = core;
		mController = controller;
		mJobMode = ChainMode.DEFAULT;
		mLogger = logger;
		mDeadline = -1;
	}

	/**
	 * Write all IResults produced by the toolchain to the logger.
	 */
	protected void logResults() {
		if (mServices == null) {
			return;
		}
		mLogger.info(" --- Results ---");
		for (final Entry<String, List<IResult>> entry : mServices.getResultService().getResults().entrySet()) {
			mLogger.info(String.format(" * Results from %s:", entry.getKey()));

			for (final IResult result : entry.getValue()) {
				final StringBuilder sb = new StringBuilder();

				sb.append("  - ");
				sb.append(result.getClass().getSimpleName());
				if (result instanceof IResultWithLocation) {
					sb.append(" [Line: ");
					final ILocation loc = ((IResultWithLocation) result).getLocation();
					sb.append(loc.getStartLine()).append("]");
				}
				sb.append(": ");
				sb.append(result.getShortDescription());
				mLogger.info(sb.toString());

				final boolean appendCompleteLongDescription = mServices.getPreferenceProvider(Activator.PLUGIN_ID)
						.getBoolean(CorePreferenceInitializer.LABEL_LONG_RESULT);
				final String[] s = result.getLongDescription().split("\n");
				if (appendCompleteLongDescription) {
					mLogger.info(String.format("    %s", result.getLongDescription()));
				} else {
					mLogger.info(String.format("    %s", s[0].replaceAll("\\n|\\r", "")));
					if (s.length > 1) {
						mLogger.info("    [...]");
					}
				}

			}
		}
	}

	private void setTimeout() {
		long realDeadline = 0;

		final IPreferenceProvider ups = mServices.getPreferenceProvider(Activator.PLUGIN_ID);
		final int preferencesDeadline = ups.getInt(CorePreferenceInitializer.LABEL_TIMEOUT);

		// first , check if we have a deadline set by the executor
		if (mDeadline != -1) {
			// mDeadline is in ms
			realDeadline = mDeadline;
		} else {
			// preferenceDeadline is in s
			realDeadline = preferencesDeadline * 1000L;
		}

		if (realDeadline > 0) {
			// only set a timeout if there is a non-zero positive value
			mServices.getProgressMonitorService().setDeadline(System.currentTimeMillis() + realDeadline);
		}
	}

	protected void setServices(IUltimateServiceProvider services) {
		mServices = services;
		setTimeout();
	}

	/**
	 * Set a deadline in ms after which the toolchain should stop. All values smaller than 0 will be ignored. 0 disables
	 * all timeouts.
	 * 
	 * @param deadline
	 *            The deadline in ms
	 */
	public void setDeadline(long deadline) {
		if (deadline >= 0) {
			mDeadline = deadline;
		}
	}

	@Override
	protected final IStatus run(IProgressMonitor monitor) {
		switch (mJobMode) {
		case RERUN:
			return rerunToolchain(monitor);
		case DEFAULT:
			return runToolchainDefault(monitor);
		case KEEP_INPUT:
			return runToolchainKeepInput(monitor);
		case KEEP_Toolchain:
			return runToolchainKeepToolchain(monitor);
		default:
			throw new UnsupportedOperationException();
		}
	}

	protected abstract IStatus runToolchainKeepToolchain(IProgressMonitor monitor);

	protected abstract IStatus runToolchainKeepInput(IProgressMonitor monitor);

	protected abstract IStatus rerunToolchain(IProgressMonitor monitor);

	protected abstract IStatus runToolchainDefault(IProgressMonitor monitor);

	protected IStatus convert(final ReturnCode result) {
		switch (result) {
		case Ok:
			return Status.OK_STATUS;
		case Cancel:
			return Status.CANCEL_STATUS;
		case Error:
		default:
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, result.toString(), null);
		}
	}
}
