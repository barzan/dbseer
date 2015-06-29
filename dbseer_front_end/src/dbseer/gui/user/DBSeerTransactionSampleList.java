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
