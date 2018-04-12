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

package middleware;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoon on 2014. 7. 5..
 */
public class SQLStatementAnalyzer implements Visitor
{
	List<String> tables;
  int type;

	public SQLStatementAnalyzer()
	{
		tables = new ArrayList<String>();
    type = LiveTransactionLocation.NONE;
	}

	public void initialize()
	{
		tables.clear();
	}

	@Override
	public Visitable visit(Visitable visitable) throws StandardException
	{
		if (visitable instanceof FromTable)
		{
			FromTable from = (FromTable)visitable;

			if (from.getOrigTableName() != null)
				tables.add(from.getOrigTableName().getFullTableName());

			if (type == LiveTransactionLocation.NONE)
			{
        type = LiveTransactionLocation.SELECT;
			}
		}
		else if (visitable instanceof InsertNode)
		{
			InsertNode node = (InsertNode)visitable;

			if (node.getTargetTableName() != null)
				tables.add(node.getTargetTableName().getFullTableName());

      type = LiveTransactionLocation.INSERT;
		}
		else if (visitable instanceof UpdateNode)
		{
			UpdateNode node = (UpdateNode)visitable;

			if (node.getTargetTableName() != null)
				tables.add(node.getTargetTableName().getFullTableName());

      type = LiveTransactionLocation.UPDATE;
		}
		else if (visitable instanceof DeleteNode)
		{
			DeleteNode node = (DeleteNode)visitable;

			if (node.getTargetTableName() != null)
				tables.add(node.getTargetTableName().getFullTableName());

      type = LiveTransactionLocation.DELETE;
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

  public int getType()
  {
    return type;
  }
}
