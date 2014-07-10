package dbseer.comp;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.*;
import dbseer.gui.DBSeerConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoon on 2014. 7. 5..
 */
public class SQLStatementAnalyzer implements Visitor
{
	List<String> tables;
	int mode;

	public SQLStatementAnalyzer()
	{
		tables = new ArrayList<String>();
		mode = DBSeerConstants.STATEMENT_NONE;
	}

	public void initialize()
	{
		tables.clear();
		mode = DBSeerConstants.STATEMENT_NONE;
	}

	@Override
	public Visitable visit(Visitable visitable) throws StandardException
	{
		if (visitable instanceof FromTable)
		{
			FromTable from = (FromTable)visitable;

			if (from.getOrigTableName() != null)
				tables.add(from.getOrigTableName().getFullTableName());

			if (mode == DBSeerConstants.STATEMENT_NONE)
			{
				mode = DBSeerConstants.STATEMENT_READ;
			}
		}
		else if (visitable instanceof InsertNode)
		{
			InsertNode node = (InsertNode)visitable;

			if (node.getTargetTableName() != null)
				tables.add(node.getTargetTableName().getFullTableName());

			mode = DBSeerConstants.STATEMENT_INSERT;
		}
		else if (visitable instanceof UpdateNode)
		{
			UpdateNode node = (UpdateNode)visitable;

			if (node.getTargetTableName() != null)
				tables.add(node.getTargetTableName().getFullTableName());

			mode = DBSeerConstants.STATEMENT_UPDATE;
		}
		else if (visitable instanceof DeleteNode)
		{
			DeleteNode node = (DeleteNode)visitable;

			if (node.getTargetTableName() != null)
				tables.add(node.getTargetTableName().getFullTableName());

			mode = DBSeerConstants.STATEMENT_DELETE;
		}
		return null;
	}

	@Override
	public boolean visitChildrenFirst(Visitable visitable)
	{
		return false;
	}

	@Override
	public boolean stopTraversal()
	{
		return false;
	}

	@Override
	public boolean skipChildren(Visitable visitable) throws StandardException
	{
		return false;
	}

	public List<String> getTables()
	{
		return tables;
	}

	public int getMode()
	{
		return mode;
	}
}
