package dbseer.gui;

/**
 * Created by dyoon on 2014. 6. 17..
 */
public class DBSeerConstants
{
	// prediction test modes
	public static final int TEST_MODE_MIXTURE_TPS = 0;
	public static final int TEST_MODE_DATASET = 1;

	// Grouping Type Constants
	public static final int GROUP_NONE = 0;
	public static final int GROUP_RANGE = 1;
	public static final int GROUP_REL_DIFF = 2;
	public static final int GROUP_NUM_CLUSTER = 3;
	public static final String[] GROUP_TYPES = {"None", "Group by range", "Group by relative diff",
			"Group by clustering"};

	// Grouping Target Constants
	public static final int GROUP_TARGET_INDIVIDUAL_TRANS_COUNT = 0;
	public static final int GROUP_TARGET_TPS = 1;
	public static final String[] GROUP_TARGETS = {"Individual transactions", "TPS"};

	// lock types
	public static final int LOCK_WAITTIME = 0;
	public static final int LOCK_NUMLOCKS = 1;
	public static final int LOCK_NUMCONFLICTS = 2;
	public static final String[] LOCK_TYPES = {"Wait Time", "Number of Locks", "Number of Conflicts"};
	public static final String[] LEARN_LOCK = {"True", "False"};

	public static final String RAW_DATASET_PATH = "./dataset";

	// Statement types
	public static final int STATEMENT_NONE = 0;
	public static final int STATEMENT_READ = 1;
	public static final int STATEMENT_INSERT = 2;
	public static final int STATEMENT_UPDATE = 3;
	public static final int STATEMENT_DELETE = 4;

	// Explain types
	public static final int EXPLAIN_SELECT_NORMAL_REGION = 0;
	public static final int EXPLAIN_SELECT_ANOMALY_REGION = 1;
	public static final int EXPLAIN_CLEAR_REGION = 2;
	public static final int EXPLAIN_EXPLAIN = 3;
	public static final int EXPLAIN_TOGGLE_PREDICATES = 4;
	public static final int EXPLAIN_SAVE_PREDICATES = 5;
	public static final int EXPLAIN_APPEND_NORMAL_REGION = 6;
	public static final int EXPLAIN_APPEND_ANOMALY_REGION = 7;
	public static final int EXPLAIN_UPDATE_EXPLANATIONS = 8;

	// Constants
	public static final double EXPLAIN_DEFAULT_CONFIDENCE_THRESHOLD = 20.0;
	public static final int DBSCAN_MAX_CLUSTERS = 5;

	public static final int MAX_PREDICTION_TPS = 10000;
	public static final int MIN_PREDICTION_TPS = 0;

	// chart types
	public static final int CHART_XYLINE = 0;
	public static final int CHART_BAR = 1;

	// performance analysis types
	public static final int ANALYSIS_WHATIF = 0;
	public static final int ANALYSIS_BOTTLENECK = 1;
	public static final int ANALYSIS_BLAME = 2;
	public static final int ANALYSIS_THROTTLING = 3;

}

