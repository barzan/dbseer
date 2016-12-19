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
import matlabcontrol.*;

/**
 * Created by dyoon on 5/26/15.
 */
public class MatlabRunner extends StatisticalPackageRunner
{
	private MatlabProxy proxy;

	private static MatlabRunner runner = null;

	private static MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
			.setUsePreviouslyControlledSession(true)
			.setHidden(false).build();

	private MatlabRunner()
	{
		MatlabProxyFactory factory = new MatlabProxyFactory(options);

		try
		{
			proxy = factory.getProxy();
			proxy.eval("clear all");
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public static synchronized MatlabRunner getInstance()
	{
		if (runner == null)
		{
			runner = new MatlabRunner();
		}
		return runner;
	}

	public void resetProxy() throws Exception
	{
		MatlabProxyFactory factory = new MatlabProxyFactory(options);

		if (proxy.isConnected())
		{
			proxy.exit();
		}
		proxy = factory.getProxy();
		runner.eval("clear all");
	}


	@Override
	public boolean eval(String str) throws Exception
	{
		proxy.eval(str);
		return true;
//		try
//		{
//			proxy.eval(str);
//		}
//		catch (MatlabInvocationException e)
//		{
//			DBSeerExceptionHandler.handleException(e);
//			return false;
//		}
//		return true;
	}

	@Override
	public double[] getVariableDouble(String var)
	{
		try
		{
			return (double[])proxy.getVariable(var);
		}
		catch (MatlabInvocationException e)
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
			return proxy.getVariable(var);
		}
		catch (MatlabInvocationException e)
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
			return (String)proxy.getVariable(var);
		}
		catch (MatlabInvocationException e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return null;
	}

	public MatlabProxy getProxy()
	{
		return proxy;
	}
}
