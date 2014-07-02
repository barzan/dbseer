package dbseer.gui.comp;

import com.foundationdb.sql.parser.SQLParser;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by dyoon on 2014. 7. 2..
 */
public class SQLAnalyzer
{
	private Set<String> tableSet;
	private SQLParser parser;

	public SQLAnalyzer()
	{
		tableSet = new HashSet<String>();
		parser = new SQLParser();
	}

	public void analyzeStatement(String statement)
	{
		// TODO
	}
}
