package de.uni_freiburg.informatik.ultimate.icfgtransformer.heapseparator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.core.model.models.ModelUtils;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.icfgtransformer.AxiomsAdderIcfgTransformer;
import de.uni_freiburg.informatik.ultimate.icfgtransformer.IBacktranslationTracker;
import de.uni_freiburg.informatik.ultimate.icfgtransformer.IIcfgTransformer;
import de.uni_freiburg.informatik.ultimate.icfgtransformer.ILocationFactory;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.absint.vpdomain.CongruenceClosureSmtUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.absint.vpdomain.HeapSepProgramConst;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IIcfg;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IcfgEdgeIterator;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IcfgLocation;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transformations.ReplacementVarFactory;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramConst;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramNonOldVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVarOrConst;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.ProgramVarUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtSortUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.equalityanalysis.IEqualityAnalysisResultProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.equalityanalysis.IEqualityProvidingIntermediateState;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.util.datastructures.DataStructureUtils;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.NestedMap2;

public class HeapSepIcfgTransformer<INLOC extends IcfgLocation, OUTLOC extends IcfgLocation>
		implements IIcfgTransformer<OUTLOC> {

	private IIcfg<OUTLOC> mResultIcfg;

	/**
	 * The IProgramVarOrConsts that model the heap in our memory model.
	 */
	private final List<IProgramVarOrConst> mHeapArrays;

	private final ILogger mLogger;

	private final HeapSeparatorBenchmark mStatistics;

	private final ManagedScript mMgdScript;

	private final HeapSepSettings mSettings;


	/**
	 * prefix of heap arrays (copied from class "SFO" in C to Boogie translation)
	 */
	public static final String MEMORY = "#memory";


	public static final String MEMLOC = "##memloc";
	public static final String MEMLOC_SORT_INT = "##mmlc_sort_int";

	/**
	 * Default constructor.
	 *
	 * @param originalIcfg
	 *            an input {@link IIcfg}.
	 * @param funLocFac
	 *            A location factory.
	 * @param backtranslationTracker
	 *            A backtranslation tracker.
	 * @param outLocationClass
	 *            The class object of the type of locations of the output {@link IIcfg}.
	 * @param newIcfgIdentifier
	 *            The identifier of the new {@link IIcfg}
	 * @param validArray
	 * @param statistics
	 * @param transformer
	 *            The transformer that should be applied to each transformula of each transition of the input
	 *            {@link IIcfg} to create a new {@link IIcfg}.
	 */
	public HeapSepIcfgTransformer(final ILogger logger, final IIcfg<INLOC> originalIcfg,
			final ILocationFactory<INLOC, OUTLOC> funLocFac,
			final ReplacementVarFactory replacementVarFactory, final IBacktranslationTracker backtranslationTracker,
			final Class<OUTLOC> outLocationClass, final String newIcfgIdentifier,
			final IEqualityAnalysisResultProvider<IcfgLocation, IIcfg<?>> equalityProvider,
			final IProgramNonOldVar validArray) {
		assert logger != null;
		mStatistics = new HeapSeparatorBenchmark();
		mMgdScript = originalIcfg.getCfgSmtToolkit().getManagedScript();
		mLogger = logger;

		mSettings = new HeapSepSettings();

		// TODO: complete, make nicer..
//		final List<String> heapArrayNames = Arrays.asList("#memory_int", "memory_$Pointer$");
		mHeapArrays = originalIcfg.getCfgSmtToolkit().getSymbolTable().getGlobals().stream()
				.filter(pvoc -> pvoc.getGloballyUniqueId().startsWith(MEMORY)).collect(Collectors.toList());

		mLogger.info("HeapSepIcfgTransformer: Starting heap partitioning");
		mLogger.info("To be partitioned heap arrays found " + mHeapArrays);

		computeResult(originalIcfg, funLocFac, replacementVarFactory, backtranslationTracker, outLocationClass,
				newIcfgIdentifier, equalityProvider, validArray);
	}

	/**
	 * Steps in the transformation:
	 * <ul>
	 *  <li> two options for preprocessing
	 *   <ol>
	 *    <li> execute the ArrayIndexExposer: transform the input Icfg into an Icfg with additional "freeze-variables"
	 *    <li> introduce the "memloc"-array
	 *   </ol>
	 *  <li> run the equality analysis (VPDomain/map equality domain) on the preprocessed Icfg
	 *  <li> compute an array partitioning according to the analysis result
	 *  <li> transform the input Icfg into an Icfg where the arrays have been split
	 * </ul>
	 *
	 * @param originalIcfg
	 * @param funLocFac
	 * @param replacementVarFactory
	 * @param backtranslationTracker
	 * @param outLocationClass
	 * @param newIcfgIdentifier
	 * @param equalityProvider
	 * @param validArray
	 * @return
	 */
	private void computeResult(final IIcfg<INLOC> originalIcfg, final ILocationFactory<INLOC, OUTLOC> funLocFac,
			final ReplacementVarFactory replacementVarFactory, final IBacktranslationTracker backtranslationTracker,
			final Class<OUTLOC> outLocationClass, final String newIcfgIdentifier,
			final IEqualityAnalysisResultProvider<IcfgLocation, IIcfg<?>> equalityProvider,
			final IProgramNonOldVar validArray) {


		final ILocationFactory<OUTLOC, OUTLOC> outToOutLocFac =
				(ILocationFactory<OUTLOC, OUTLOC>) createIcfgLocationToIcfgLocationFactory();

		/*
		 * 1. Execute the preprocessing
		 */
		final IIcfg<OUTLOC> preprocessedIcfg;
		final NestedMap2<EdgeInfo, Term, StoreIndexInfo> edgeToIndexToStoreIndexInfo;
		final Map<StoreIndexInfo, IProgramNonOldVar> storeIndexInfoToFreezeVar;
		final Map<StoreIndexInfo, IProgramConst> storeIndexInfoToLocLiteral;
		final IProgramNonOldVar memlocArrayInt;
		final Sort memLocSort;
		if (mSettings.getPreprocessing() == Preprocessing.FREEZE_VARIABLES) {
			mLogger.info("starting freeze-var-style preprocessing");
			/*
			 * add the freeze var updates to each transition with an array update
			 */
			final StoreIndexFreezerIcfgTransformer<INLOC, OUTLOC> sifit =
					new StoreIndexFreezerIcfgTransformer<>(mLogger, "icfg_with_uninitialized_freeze_vars",
							outLocationClass, originalIcfg, funLocFac, backtranslationTracker, mHeapArrays);
			IIcfg<OUTLOC> icfgWFreezeVarsUninitialized = sifit.getResult();

			storeIndexInfoToFreezeVar = sifit.getArrayAccessInfoToFreezeVar();
			edgeToIndexToStoreIndexInfo = sifit.getEdgeToIndexToStoreIndexInfo();

			mLogger.info("finished StoreIndexFreezer, created " + storeIndexInfoToFreezeVar.size() + " freeze vars and "
					+ "freeze var literals (each corresponds to one heap write)");

			/*
			 * Create a fresh literal/constant for each freeze variable that was introduced, we call them freeze
			 * literals.
			 * Announce them to the equality analysis as special literals, which are, by axiom, pairwise disjoint.
			 */
			final Map<IProgramNonOldVar, IProgramConst> freezeVarTofreezeVarLit = new HashMap<>();

			mMgdScript.lock(this);
			for (final IProgramNonOldVar freezeVar : storeIndexInfoToFreezeVar.values()) {

				final String freezeVarLitName = getFreezeVarLitName(freezeVar);
				mMgdScript.declareFun(this, freezeVarLitName, new Sort[0], freezeVar.getSort());
				final ApplicationTerm freezeVarLitTerm = (ApplicationTerm) mMgdScript.term(this, freezeVarLitName);

				freezeVarTofreezeVarLit.put(freezeVar, new HeapSepProgramConst(freezeVarLitTerm));
			}
			mMgdScript.unlock(this);

			// make sure the literals are all treated as pairwise unequal
			final Collection<IProgramConst> freezeVarLits = freezeVarTofreezeVarLit.values();
			final Set<ConstantTerm> allConstantTerms = sifit.getAllConstantTerms();
			final Set<Term> literalTerms = new HashSet<>();
				literalTerms.addAll(freezeVarLits.stream()
						.map(pvoc -> pvoc.getTerm())
						.collect(Collectors.toList()));
				literalTerms.addAll(allConstantTerms);


			equalityProvider.announceAdditionalLiterals(freezeVarLits);
			if (mSettings.isAssertFreezeVarLitDisequalitiesIntoScript()) {
				/*
				 * TODO: this is something between non-elegant and highly problematic -- make the axiom-style solution
				 * work!
				 */
				assertLiteralDisequalitiesIntoScript(literalTerms);
			}

			if (mSettings.isAddLiteralDisequalitiesAsAxioms()) {

				final Term allLiteralDisequalities = SmtUtils.and(mMgdScript.getScript(),
						CongruenceClosureSmtUtils.createDisequalityTermsForNonTheoryLiterals(mMgdScript.getScript(),
								literalTerms));

				icfgWFreezeVarsUninitialized = new AxiomsAdderIcfgTransformer<>( mLogger,
						"icfg_with_uninitialized_freeze_vars_and_literal_axioms", outLocationClass,
						icfgWFreezeVarsUninitialized, outToOutLocFac, backtranslationTracker, allLiteralDisequalities)
						.getResult();
			}

			/*
			 * Add initialization code for each of the newly introduced freeze variables.
			 * Each freeze variable is initialized to its corresponding freeze literal.
			 * Furthermore the valid-array (of the memory model) is assumed to be 1 at each freeze literal.
			 */
			final FreezeVarInitializer<OUTLOC, OUTLOC> fvi = new FreezeVarInitializer<>(mLogger,
					"icfg_with_initialized_freeze_vars", outLocationClass, icfgWFreezeVarsUninitialized, outToOutLocFac,
					backtranslationTracker, freezeVarTofreezeVarLit, validArray, mSettings);
			final IIcfg<OUTLOC> icfgWFreezeVarsInitialized = fvi.getResult();

			preprocessedIcfg = icfgWFreezeVarsInitialized;

			storeIndexInfoToLocLiteral = null;
			memlocArrayInt = null;
		} else {
			assert mSettings.getPreprocessing() == Preprocessing.MEMLOC_ARRAY;
			mLogger.info("Heap separator: starting memloc-array-style preprocessing");

			/**
			 * create program variable for memloc array
			 *  conceptually, we need one memloc array for each index sort that is used in the program, for now we just
			 *  create one for integer indices
			 */

			mMgdScript.lock(this);
			mMgdScript.getScript().declareSort(MEMLOC_SORT_INT, 0);
			memLocSort = mMgdScript.getScript().sort(MEMLOC_SORT_INT);
			final Sort intToLocations = SmtSortUtils.getArraySort(mMgdScript.getScript(),
					SmtSortUtils.getIntSort(mMgdScript), memLocSort);
			memlocArrayInt = ProgramVarUtils.constructGlobalProgramVarPair(MEMLOC + "_int", intToLocations, mMgdScript,
					this);
			mMgdScript.unlock(this);


			/*
			 * add the memloc array updates to each transition with an array update
			 * the values the memloc array is set to are location literals, those are pairwise different by axiom
			 */
			final MemlocArrayUpdaterIcfgTransformer<INLOC, OUTLOC> mauit =
					new MemlocArrayUpdaterIcfgTransformer<>(mLogger, "icfg_with_memloc_updates",
							outLocationClass, originalIcfg, funLocFac, backtranslationTracker, memlocArrayInt,
							memLocSort, mHeapArrays);
			IIcfg<OUTLOC> icfgWithMemlocUpdates = mauit.getResult();

			edgeToIndexToStoreIndexInfo = mauit.getEdgeToIndexToStoreIndexInfo();
			storeIndexInfoToLocLiteral = mauit.getStoreIndexInfoToLocLiteral();

			mLogger.info("finished MemlocArrayUpdater, created " + mauit.getLocationLiterals().size() +
					" location literals (each corresponds to one heap write)");

			// make sure the literals are all treated as pairwise unequal
			equalityProvider.announceAdditionalLiterals(mauit.getLocationLiterals());

			final Set<Term> literalTerms = mauit.getLocationLiterals().stream()
						.map(pvoc -> pvoc.getTerm())
						.collect(Collectors.toSet());
			if (mSettings.isAssertFreezeVarLitDisequalitiesIntoScript()) {
				/*
				 * TODO: this is somewhere between inelegant and highly problematic -- make the axiom-style solution
				 * work!
				 */
				assertLiteralDisequalitiesIntoScript(literalTerms);
			}
			if (mSettings.isAddLiteralDisequalitiesAsAxioms()) {

				final Term allLiteralDisequalities = SmtUtils.and(mMgdScript.getScript(),
						CongruenceClosureSmtUtils.createDisequalityTermsForNonTheoryLiterals(mMgdScript.getScript(),
								literalTerms));

				icfgWithMemlocUpdates = new AxiomsAdderIcfgTransformer<>( mLogger,
						"icfg_with_memloc_updates_and_literal_axioms", outLocationClass,
						icfgWithMemlocUpdates, outToOutLocFac, backtranslationTracker, allLiteralDisequalities)
						.getResult();
			}

			preprocessedIcfg = icfgWithMemlocUpdates;
			storeIndexInfoToFreezeVar = null;
		}
		mLogger.info("finished preprocessing for the equality analysis");
		if (mSettings.getPreprocessing() == Preprocessing.FREEZE_VARIABLES) {
			mLogger.debug("storeIndexInfoToFreezeVar: " + DataStructureUtils.prettyPrint(storeIndexInfoToFreezeVar));
		} else {
			mLogger.debug("storeIndexInfoToLocLiteral: " + DataStructureUtils.prettyPrint(storeIndexInfoToLocLiteral));
		}
		mLogger.debug("edgeToIndexToStoreIndexInfo: " + DataStructureUtils.prettyPrint(edgeToIndexToStoreIndexInfo));

		/*
		 * 2. run the equality analysis
		 */
		equalityProvider.preprocess(preprocessedIcfg);
		mLogger.info("finished equality analysis");


		/*
		 * 3a.
		 */
		final HeapSepPreAnalysis heapSepPreanalysis = new HeapSepPreAnalysis(mLogger, mMgdScript, mHeapArrays,
				mStatistics);
		new IcfgEdgeIterator(originalIcfg).forEachRemaining(edge -> heapSepPreanalysis.processEdge(edge));
		heapSepPreanalysis.finish();
		mLogger.info("Finished pre analysis before partitioning");
		mLogger.info("  array groups: " + DataStructureUtils.prettyPrint(
				new HashSet<>(heapSepPreanalysis.getArrayToArrayGroup().values())));
		mLogger.info("  select infos: " + DataStructureUtils.prettyPrint(heapSepPreanalysis.getSelectInfos()));

		final Map<IProgramVarOrConst, ArrayGroup> arrayToArrayGroup = heapSepPreanalysis.getArrayToArrayGroup();

		final HeapPartitionManager partitionManager;
		if (mSettings.getPreprocessing() == Preprocessing.FREEZE_VARIABLES) {
			partitionManager = new HeapPartitionManager(mLogger, arrayToArrayGroup, storeIndexInfoToFreezeVar,
					mHeapArrays, mStatistics, mMgdScript);
		} else {
			assert mSettings.getPreprocessing() == Preprocessing.MEMLOC_ARRAY;
			partitionManager = new HeapPartitionManager(mLogger, mMgdScript, arrayToArrayGroup, mHeapArrays,
					mStatistics, memlocArrayInt, storeIndexInfoToLocLiteral);
		}

		/*
		 * 3b. compute an array partitioning
		 */
		for (final SelectInfo si : heapSepPreanalysis.getSelectInfos()) {
			partitionManager.processSelect(si, getEqualityProvidingIntermediateState(si.getEdgeInfo(),
					equalityProvider));
		}
		partitionManager.finish();


		/*
		 * 4. Execute the transformer that splits up the arrays according to the result from the equality analysis.
		 *  Note that this transformation is done on the original input Icfg, not on the output of the
		 *  ArrayIndexExposer, which we ran the equality analysis on.
		 */
		final PartitionProjectionTransitionTransformer<INLOC, OUTLOC> heapSeparatingTransformer =
				new PartitionProjectionTransitionTransformer<>(mLogger, "HeapSeparatedIcfg", outLocationClass,
						originalIcfg, funLocFac, backtranslationTracker,
						partitionManager.getSelectInfoToDimensionToLocationBlock(),
						edgeToIndexToStoreIndexInfo,
						arrayToArrayGroup,
						mHeapArrays,
						mStatistics);
		mResultIcfg = heapSeparatingTransformer.getResult();
	}

	public void assertLiteralDisequalitiesIntoScript(final Set<Term> literalTerms) {
		mMgdScript.lock(this);
		final Term allLiteralDisequalities = SmtUtils.and(mMgdScript.getScript(),
				CongruenceClosureSmtUtils.createDisequalityTermsForNonTheoryLiterals(
						mMgdScript.getScript(), literalTerms));
		mMgdScript.assertTerm(this, allLiteralDisequalities);
		mMgdScript.unlock(this);
	}

	private String getFreezeVarLitName(final IProgramNonOldVar freezeVar) {
		// TODO make _really_ sure that the new id is unique
		return freezeVar.getGloballyUniqueId() + "_lit";
	}

	/**
	 * For the moment this will return the EqState of the source location of edgeInfo, but in order to be able to
	 *  deal with select indices that are aux vars, we need to have something different here
	 * @param edgeInfo
	 * @param equalityProvider
	 * @return
	 */
	private IEqualityProvidingIntermediateState getEqualityProvidingIntermediateState(final EdgeInfo edgeInfo,
			final IEqualityAnalysisResultProvider<IcfgLocation, IIcfg<?>> equalityProvider) {
		return equalityProvider.getEqualityProvidingIntermediateState(edgeInfo.getEdge());
	}

	@Override
	public IIcfg<OUTLOC> getResult() {
		return mResultIcfg;
	}


	public HeapSeparatorBenchmark getStatistics() {
		return mStatistics;
	}

	/**
	 * (almost) a copy from IcfgTransformationObserver
	 *  --> should probably replace this with a less ad-hoc solution some time
	 *
	 * @return
	 */
	private static ILocationFactory<BoogieIcfgLocation, BoogieIcfgLocation> createIcfgLocationToIcfgLocationFactory() {
		return (oldLocation, debugIdentifier, procedure) -> {
				final BoogieIcfgLocation rtr = new BoogieIcfgLocation(debugIdentifier, procedure,
					oldLocation.isErrorLocation(), oldLocation.getBoogieASTNode());
			ModelUtils.copyAnnotations(oldLocation, rtr);
			return rtr;
		};
	}
}

enum Preprocessing {
	FREEZE_VARIABLES, MEMLOC_ARRAY;
}

class HeapSepSettings {
	/**
	 *
	 * not clear:
	 *  <li> how much of a slowdown this causes
	 *  <li> if it is only necessary for assertions or not
	 */
	private final boolean mAssumeFreezeVarLitDisequalitiesAtInitEdges = false;

	private final boolean mAssertFreezeVarLitDisequalitiesIntoScript = true;

	private final Preprocessing mPreprocessing = Preprocessing.MEMLOC_ARRAY;
//	private final Preprocessing mPreprocessing = Preprocessing.FREEZE_VARIABLES;

	private final boolean mAddLiteralDisequalitiesAsAxioms = false;

	public boolean isAssumeFreezeVarLitDisequalitiesAtInitEdges() {
		return mAssumeFreezeVarLitDisequalitiesAtInitEdges;
	}

	public boolean isAddLiteralDisequalitiesAsAxioms() {
		return mAddLiteralDisequalitiesAsAxioms;
	}

	public boolean isAssertFreezeVarLitDisequalitiesIntoScript() {
		return mAssertFreezeVarLitDisequalitiesIntoScript;
	}

	public Preprocessing getPreprocessing() {
		return mPreprocessing;
	}
}
