package dbseer.gui;

/**
 * Created by dyoon on 2014. 6. 17..
 */
public class DBSeerConstants
{
	// prediction test modes
	public static final int TEST_MODE_DATASET = 0;
	public static final int TEST_MODE_MIXTURE_TPS = 1;

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

}
