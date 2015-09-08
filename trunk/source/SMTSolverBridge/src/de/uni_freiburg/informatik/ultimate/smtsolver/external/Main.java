/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 Oday Jubran
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE SMTSolverBridge.
 * 
 * The ULTIMATE SMTSolverBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE SMTSolverBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE SMTSolverBridge. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE SMTSolverBridge, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE SMTSolverBridge grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.smtsolver.external;

import java.io.IOException;
import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.logic.SMTLIBException;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.SMTInterpol;
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.ParseEnvironment;

public class Main {

	private static void usage() {
		System.err.println("USAGE smtinterpol [-q] [-v] [-t <num>] [-r <num>] [file.smt2]");
	}

	public static void main(String[] param) throws IOException {

		/** Specify the solver command here. **/
		String command = "z3 -smt2 -in";

		Logger logger = Logger.getRootLogger();
		int paramctr = 0;
		Script benchmark;
		if (!command.equals("SMTInterpol"))
			benchmark = new Scriptor(command, logger, null, null);
		else
			benchmark = new SMTInterpol(logger, true);

		while (paramctr < param.length && param[paramctr].startsWith("-")) {
			if (param[paramctr].equals("--")) {
				paramctr++;
				break;
			} else if (param[paramctr].equals("-v")) {
				try {
					benchmark.setOption(":verbosity", BigInteger.valueOf(5));
				} catch (SMTLIBException doesNotHappen) {
				}
				paramctr++;
			} else if (param[paramctr].equals("-q")) {
				try {
					benchmark.setOption(":verbosity", BigInteger.valueOf(3));
				} catch (SMTLIBException doesNotHappen) {
				}
				paramctr++;
			} else if (param[paramctr].equals("-t") && ++paramctr < param.length) {
				try {
					int timeout = Integer.parseInt(param[paramctr]);
					if (timeout < 0) {
						logger.error("Cannot parse timeout " + "argument: Negative number");
					} else {
						try {
							benchmark.setOption(":timeout", BigInteger.valueOf(timeout));
						} catch (SMTLIBException doesNotHappen) {
						}
					}
				} catch (NumberFormatException nfe) {
					logger.error("Cannot parse timeout " + "argument: Not a number");
				}
				paramctr++;
			} else if (param[paramctr].equals("-r") && ++paramctr < param.length) {
				try {
					int seed = Integer.parseInt(param[paramctr]);
					if (seed < 0) {
						logger.error("Cannot parse random seed " + "argument: Negative number");
					} else {
						try {
							benchmark.setOption(":random-seed", BigInteger.valueOf(seed));
						} catch (SMTLIBException doesNotHappen) {
						}
					}
				} catch (NumberFormatException nfe) {
					logger.error("Cannot parse random seed " + "argument: Not a number");
				}
				paramctr++;
			} else {
				usage();
				return;
			}
		}
		String filename;
		if (paramctr < param.length) {
			filename = param[paramctr++];
		} else {
			filename = "<stdin>";
		}
		if (paramctr != param.length) {
			usage();
			return;
		}
		ParseEnvironment parseEnv = new ParseEnvironment(benchmark);
		try {
			parseEnv.parseScript(filename);
		} catch (SMTLIBException exc) {
			parseEnv.printError(exc.getMessage());
		}
	}
}
