package dbseer.gui;

import java.util.ArrayList;

/**
 * Created by dyoon on 2014. 6. 2..
 */
public class DBSeerConfiguration
{
	private String name = "";

	// Configuration consists of multiple descriptions.
	private ArrayList<DBSeerDataProfile> profileArray;

	public DBSeerConfiguration()
	{
		profileArray = new ArrayList<DBSeerDataProfile>();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
