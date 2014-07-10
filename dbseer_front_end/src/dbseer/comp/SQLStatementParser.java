package dbseer.comp;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.SQLParser;
import com.foundationdb.sql.parser.StatementNode;
import com.foundationdb.sql.parser.Visitable;
import com.foundationdb.sql.parser.Visitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dyoon on 2014. 7. 2..
 */
public class SQLStatementParser
{
	private SQLParser parser;
	private SQLStatementAnalyzer analyzer;

	public SQLStatementParser()
	{
		parser = new SQLParser();
		analyzer = new SQLStatementAnalyzer();
	}

	public int parseStatement(String statement)
	{
		try
		{
			analyzer.initialize();
			List<StatementNode> statements = parser.parseStatements(statement);
			for (StatementNode stmt : statements)
			{
				stmt.accept(analyzer);
			}
		}
		catch (StandardException e)
		{
//			e.printStackTrace();
		}
		return analyzer.getMode();
	}

	public List<String> getTables()
	{
		return analyzer.getTables();
	}
}
