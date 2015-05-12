package de.uni_freiburg.informatik.ultimatetest;

import static org.junit.Assert.fail;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;

import de.uni_freiburg.informatik.junit_helper.testfactory.FactoryTestMethod;
import de.uni_freiburg.informatik.ultimate.core.controllers.LivecycleException;
import de.uni_freiburg.informatik.ultimate.core.services.IResultService;
import de.uni_freiburg.informatik.ultimate.util.ExceptionUtils;
import de.uni_freiburg.informatik.ultimatetest.decider.ITestResultDecider;
import de.uni_freiburg.informatik.ultimatetest.decider.ITestResultDecider.TestResult;
import de.uni_freiburg.informatik.ultimatetest.summary.IIncrementalLog;
import de.uni_freiburg.informatik.ultimatetest.summary.ITestSummary;

/**
 * @author dietsch@informatik.uni-freiburg.de
 * 
 */
public class UltimateTestCase {

	private final String mName;
	private final UltimateRunDefinition mUltimateRunDefinition;
	private final UltimateStarter mStarter;
	private final ITestResultDecider mDecider;
	private final List<ITestSummary> mSummaries;
	private final List<IIncrementalLog> mLogs;
	private final Logger mLogger;

	public UltimateTestCase(String name, ITestResultDecider decider, UltimateStarter starter,
			UltimateRunDefinition ultimateRunDefinition, List<ITestSummary> summaries,
			List<IIncrementalLog> incrementalLogs) {
		mLogger = Logger.getLogger(UltimateStarter.class);
		mStarter = starter;
		mName = name;
		mDecider = decider;
		mSummaries = summaries;
		mUltimateRunDefinition = ultimateRunDefinition;
		mLogs = incrementalLogs;
	}

	@FactoryTestMethod
	public void test() {
		// call the garbage collector before starting a new test
		System.gc();
		System.runFinalization();
		System.gc();
		Runtime.getRuntime().gc();

		// start debug code: use this only in controlled situations!
		// try {
		// Thread.sleep(500);
		// } catch (InterruptedException e1) {
		// }
		// HeapDumper.dumpHeap("F:\\tmp\\ultimate benchmarks\\heapdump", false);
		// end debug ode

		Throwable th = null;
		TestResult result = TestResult.FAIL;
		boolean livecycleFailure = false;
		try {
			updateLogsPreStart();
			String deciderName = mDecider.getClass().getSimpleName();
			IStatus returnCode = mStarter.runUltimate();
			mLogger.info("Deciding this test: "+deciderName);
			result = mDecider.getTestResult(mStarter.getServices().getResultService());
			if (!returnCode.isOK() && result != TestResult.FAIL) {
				mLogger.fatal("#################### Overwriting decision of " + deciderName
						+ " and setting test status to FAIL ####################");
				mLogger.fatal("Ultimate returned an unexpected status:");
				mLogger.fatal("Code      " + returnCode.getCode());
				mLogger.fatal("Severity  " + returnCode.getSeverity());
				mLogger.fatal("Message   " + returnCode.getMessage());
				mLogger.fatal("Plugin ID " + returnCode.getPlugin());
				if (returnCode.getException() != null) {
					mLogger.fatal("Exception:", returnCode.getException());
				}
				result = TestResult.FAIL;
			}

		} catch (LivecycleException lex) {
			// if this happens, mStarter, mLogger, etc. are not initialized
			th = lex;
			result = mDecider.getTestResult(null, lex);
			lex.printStackTrace();
			livecycleFailure = true;
		} catch (Throwable e) {
			th = e;
			result = mDecider.getTestResult(mStarter.getServices().getResultService(), e);
			mLogger.fatal(String.format("There was an exception during the execution of Ultimate: %s%n%s", e,
					ExceptionUtils.getStackTrace(e)));
		} finally {
			boolean success = false;

			if (!livecycleFailure) {
				success = mDecider.getJUnitSuccess(result);
			}

			updateSummaries(result);
			updateLogsPostCompletion(result);
			mStarter.complete();

			if (!success) {
				String message = null;

				if (!livecycleFailure) {
					message = mDecider.getResultMessage();
				}

				if (message == null) {
					message = "ITestResultDecider provided no message";
				}
				if (th != null) {
					message += " (Ultimate threw an Exception: " + th.getMessage() + ")";
				}
				fail(message);
			}
		}
	}

	private void updateLogsPreStart() {
		if (mLogs != null) {
			for (IIncrementalLog log : mLogs) {
				log.addEntryPreStart(mUltimateRunDefinition);
			}
		}
	}

	private void updateLogsPostCompletion(TestResult result) {
		if (mLogs != null) {
			for (IIncrementalLog log : mLogs) {
				log.addEntryPostCompletion(mUltimateRunDefinition, result, mDecider.getResultCategory(),
						mDecider.getResultMessage(), mStarter.getServices());
			}
		}
	}

	private void updateSummaries(TestResult result) {

		IResultService rservice = null;
		if (mStarter.getServices() != null) {
			rservice = mStarter.getServices().getResultService();
		}

		if (mSummaries != null) {
			for (ITestSummary summary : mSummaries) {
				summary.addResult(mUltimateRunDefinition, result, mDecider.getResultCategory(),
						mDecider.getResultMessage(), mName, rservice);
			}
		}
	}

	public UltimateRunDefinition getUltimateRunDefinition() {
		return mUltimateRunDefinition;
	}

	@Override
	public String toString() {
		return mName;
	}
}