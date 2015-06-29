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

import java.util.ArrayList;

/**
 * Created by dyoon on 14. 11. 27..
 */
public class DBSeerCausalModel
{
	private ArrayList<DBSeerPredicate> predicates;
	private String cause;
	private double confidence;

	public DBSeerCausalModel(String cause, double confidence)
	{
		this.cause = cause;
		this.confidence = confidence;
		predicates = new ArrayList<DBSeerPredicate>();
	}

	public String toString()
	{
		String output = String.format("%s (%.2f%%)", cause, confidence);
		return output;
	}

	public String getCause()
	{
		return cause;
	}

	public void setCause(String cause)
	{
		this.cause = cause;
	}

	public double getConfidence()
	{
		return confidence;
	}

	public void setConfidence(double confidence)
	{
		this.confidence = confidence;
	}

	public ArrayList<DBSeerPredicate> getPredicates()
	{
		return predicates;
	}
}
