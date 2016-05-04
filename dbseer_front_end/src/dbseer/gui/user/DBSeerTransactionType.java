package dbseer.gui.user;

/**
 * Created by Dong Young Yoon on 5/4/16.
 */
public class DBSeerTransactionType
{
	private String name;
	private boolean isEnabled;

	public DBSeerTransactionType(String name, boolean isEnabled)
	{
		this.name = name;
		this.isEnabled = isEnabled;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isEnabled()
	{
		return isEnabled;
	}

	public void setEnabled(boolean enabled)
	{
		isEnabled = enabled;
	}
}
