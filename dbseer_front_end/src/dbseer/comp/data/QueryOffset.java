package dbseer.comp.data;

/**
 * Created by dyoon on 15. 1. 4..
 */
public class QueryOffset
{
	private long id;
	private long offset;

	public QueryOffset(long id, long offset)
	{
		this.id = id;
		this.offset = offset;
	}

	@Override
	public boolean equals(Object o)
	{
		QueryOffset other = (QueryOffset)o;
		return (id == other.getId());
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public long getOffset()
	{
		return offset;
	}

	public void setOffset(long offset)
	{
		this.offset = offset;
	}
}
