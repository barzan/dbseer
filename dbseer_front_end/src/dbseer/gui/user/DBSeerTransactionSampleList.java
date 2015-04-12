package dbseer.gui.user;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;

/**
 * Created by dyoon on 14. 11. 26..
 */

public class DBSeerTransactionSampleList
{
	private ArrayList<DBSeerTransactionSample> samples;

	public DBSeerTransactionSampleList()
	{
		samples = new ArrayList<DBSeerTransactionSample>();
	}

	private Object readResolve()
	{
		if (samples == null)
		{
			samples = new ArrayList<DBSeerTransactionSample>();
		}
		return this;
	}

	public ArrayList<DBSeerTransactionSample> getSamples()
	{
		return samples;
	}
}
