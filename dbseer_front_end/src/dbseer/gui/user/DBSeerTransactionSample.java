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
