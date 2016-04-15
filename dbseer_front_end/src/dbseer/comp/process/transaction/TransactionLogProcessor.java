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

package dbseer.comp.process.transaction;

import dbseer.comp.SQLStatementParser;
import dbseer.comp.data.Statement;
import dbseer.comp.data.Transaction;
import dbseer.comp.process.base.LogProcessor;
import dbseer.gui.DBSeerConstants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Dong Young Yoon on 1/2/16.
 */
public abstract class TransactionLogProcessor implements LogProcessor
{
	protected Set<String> tableNameSet;
	protected SQLStatementParser parser;

	public TransactionLogProcessor()
	{
		this.tableNameSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		this.parser = new SQLStatementParser();
	}

	public abstract List<Transaction> getTransactions(long timestamp);

	public abstract Set<Long> getTimestamps();

	protected Statement parseStatement(Transaction transaction, String statement)
	{
		Statement stmt = new Statement();
		int mode = parser.parseStatement(statement);

		if (parser.getTables().size() == 0 && !statement.toLowerCase().contains("commit") &&
				!statement.toLowerCase().contains("rollback") && !statement.toLowerCase().contains("set session"))
		{
			return null;
		}

		stmt.setContent(statement);

		if (statement.toLowerCase().contains("for update"))
		{
			mode = DBSeerConstants.STATEMENT_UPDATE;
		}

		// ignore this statement for now (OLTPBenchmark runs it at the end of the benchmark.)
		if (statement.toLowerCase().contains("select * from global_variables"))
		{
			mode = DBSeerConstants.STATEMENT_NONE;
		}

		for (String table : parser.getTables())
		{
			tableNameSet.add(table);
			Transaction.numTable = tableNameSet.size();
			stmt.addTable(table);

			int idx = Arrays.asList(tableNameSet.toArray()).indexOf(table);
			switch (mode)
			{
				case DBSeerConstants.STATEMENT_READ:
					transaction.addSelect(idx);
					break;
				case DBSeerConstants.STATEMENT_INSERT:
					transaction.addInsert(idx);
					break;
				case DBSeerConstants.STATEMENT_UPDATE:
					transaction.addUpdate(idx);
					break;
				case DBSeerConstants.STATEMENT_DELETE:
					transaction.addDelete(idx);
					break;
				default:
					break;
			}
		}

		return stmt;
	}
}
