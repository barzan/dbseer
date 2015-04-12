package dbseer.comp.data;

import dbseer.gui.DBSeerConstants;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by dyoon on 2014. 7. 5..
 */
public class Statement
{
	private int id;
	private long startTime;
	private long endTime;
	private long latency;
	private int mode;
	private Set<String> tables;
	private String content;
	private long fileOffset;

	private long queryOffset;

	public Statement()
	{
		mode = DBSeerConstants.STATEMENT_NONE;
		tables = new HashSet<String>();
	}

	public void printAll()
	{
		System.out.print("Statement: (");
		System.out.print(id + ", ");
		System.out.print(startTime + ", ");
		System.out.print(endTime + ", ");
		System.out.print(latency + ")");
		System.out.println();
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public long getLatency()
	{
		return latency;
	}

	public void setLatency(long latency)
	{
		this.latency = latency;
	}

	public int getMode()
	{
		return mode;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
	}

	public Set<String> getTables()
	{
		return tables;
	}

	public void addTable(String table)
	{
		tables.add(table);
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public long getFileOffset()
	{
		return fileOffset;
	}

	public void setFileOffset(long fileOffset)
	{
		this.fileOffset = fileOffset;
	}
	public long getQueryOffset()
	{
		return queryOffset;
	}

	public void setQueryOffset(long queryOffset)
	{
		this.queryOffset = queryOffset;
	}

}
