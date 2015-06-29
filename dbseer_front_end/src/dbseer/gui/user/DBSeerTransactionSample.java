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

package dbseer.gui.user;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by dyoon on 14. 11. 26..
 */
public class DBSeerTransactionSample
{
	private int timestamp;
	private String statement;

	public DBSeerTransactionSample(int timestamp, String statement)
	{
		this.timestamp = timestamp;
		this.statement = statement;
	}

	private Object readResolve()
	{
		return this;
	}

	public int getTimestamp()
	{
		return timestamp;
	}

	public void setTimeStamp(int timestamp)
	{
		this.timestamp = timestamp;
	}

	public String getStatement()
	{
		return statement;
	}

	public void setStatement(String statement)
	{
		this.statement = statement;
	}

}
