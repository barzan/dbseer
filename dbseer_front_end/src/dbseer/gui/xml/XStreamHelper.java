package dbseer.gui.xml;

import com.thoughtworks.xstream.XStream;
import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.user.DBSeerUserSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by dyoon on 2014. 6. 12..
 */
public class XStreamHelper
{
	private XStream xstream;

	public XStreamHelper()
	{
		xstream = new XStream();
		xstream.setMode(XStream.ID_REFERENCES);
		xstream.processAnnotations(DBSeerUserSettings.class);
		xstream.processAnnotations(DBSeerDataSet.class);
		xstream.processAnnotations(DBSeerConfiguration.class);
	}

	public Object fromXML(String xmlPath)
	{
		return xstream.fromXML(new File(xmlPath));
	}

	public void toXML(Object obj, String xmlPath) throws FileNotFoundException
	{
		xstream.toXML(obj, new FileOutputStream(new File(xmlPath)));
	}
}
