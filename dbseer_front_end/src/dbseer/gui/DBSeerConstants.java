/*
 * Copyright 2013 Barzan Mozafari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dbseer.gui;

import java.io.File;

/**
 * Created by dyoon on 2014. 6. 17..
 */
public class DBSeerConstants
{
	// DB types
	public static final int DB_MYSQL = 0;

	// OS types
	public static final int OS_LINUX = 0;

	// statistical packages
	public static final int STAT_MATLAB = 0;
	public static final int STAT_OCTAVE = 1;

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

	public static final String LIVE_DATASET_PATH = "." + File.separator + "dataset" + File.separator + "live";
	public static final String ROOT_DATASET_PATH = "." + File.separator + "dataset";

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

	public static final int MAX_NUM_TABLE = 200;
	public static final int MAX_TRANSACTION_SAMPLE = 30;

	public static final int DBSCAN_INIT_PTS = 1000;
	public static final int DBSCAN_MIN_PTS = 20;
}

