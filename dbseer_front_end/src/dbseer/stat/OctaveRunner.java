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

package dbseer.stat;

import dbseer.gui.DBSeerExceptionHandler;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.exception.OctaveEvalException;
import dk.ange.octave.type.OctaveCell;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveString;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by dyoon on 5/26/15.
 */
public class OctaveRunner extends StatisticalPackageRunner
{
	private OctaveEngine engine;

	private static OctaveRunner runner = null;

	private OctaveRunner()
	{
		engine = new OctaveEngineFactory().getScriptEngine();
		engine.eval("clear all");

		// set empty writer to suppress outputs.
		engine.setWriter(new Writer()
		{
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException
			{
			}

			@Override
			public void flush() throws IOException
			{
			}

			@Override
			public void close() throws IOException
			{
			}
		});

		// set empty writer to suppress errors.
		engine.setErrorWriter(new Writer()
		{
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException
			{

			}

			@Override
			public void flush() throws IOException
			{

			}

			@Override
			public void close() throws IOException
			{

			}
		});
	}

	public static synchronized OctaveRunner getInstance()
	{
		if (runner == null)
		{
			runner = new OctaveRunner();
		}
		return runner;
	}

	public synchronized void resetRunner()
	{
		if (engine == null)
		{
			return;
		}
		else
		{
			engine.destroy();
			engine = new OctaveEngineFactory().getScriptEngine();
			engine.eval("clear all");
		}
	}

	@Override
	public boolean eval(String str)
	{
		try
		{
			engine.eval(str);
		}
		catch (OctaveEvalException e)
		{
			DBSeerExceptionHandler.handleException(e);
			return false;
		}
		return true;
	}

	@Override
	public double[] getVariableDouble(String var)
	{
		try
		{
			OctaveDouble val = (OctaveDouble) engine.get(var);
			return val.getData();
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return null;
	}

	@Override
	public Object getVariableCell(String var)
	{
		try
		{
			OctaveCell val = (OctaveCell) engine.get(var);

			Object[] objs = val.getData();
			Object[] castedObjs = new Object[objs.length];

			int idx = 0;
			for (Object o : objs)
			{
				if (o instanceof OctaveDouble)
				{
					OctaveDouble d = (OctaveDouble)o;
					castedObjs[idx++] = d.getData();
				}
				else if (o instanceof OctaveString)
				{
					OctaveString s = (OctaveString)o;
					castedObjs[idx++] = s.getString();
				}
				else if (o instanceof OctaveCell)
				{
					OctaveCell c = (OctaveCell)o;
					castedObjs[idx++] = c.getData();
				}
				// DY: Maybe you can add more in the future...
			}
			return castedObjs;
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return null;
	}

	@Override
	public String getVariableString(String var)
	{
		try
		{
			OctaveString val = (OctaveString) engine.get(var);
			return val.getString();
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return null;
	}

	public String getVersion()
	{
		return engine.getVersion();
	}

	public OctaveEngine getEngine()
	{
		return engine;
	}
}
