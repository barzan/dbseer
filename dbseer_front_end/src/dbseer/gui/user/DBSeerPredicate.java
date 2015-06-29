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

/**
 * Created by dyoon on 14. 11. 27..
 */
public class DBSeerPredicate
{
	private String name;
	private double lowerBound;
	private double upperBound;
	private double relativeRatio;

	public DBSeerPredicate(String name, double lowerBound, double upperBound)
	{
		this.name = name;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public String toString()
	{
		if (!Double.isInfinite(lowerBound) && Double.isInfinite(upperBound))
		{
			return String.format("%s > %.2f", name, lowerBound);
		}
		else if (Double.isInfinite(lowerBound) && !Double.isInfinite(upperBound))
		{
			return String.format("%s < %.2f", name, upperBound);
		}
		else if (!Double.isInfinite(lowerBound) && !Double.isInfinite(upperBound))
		{
			return String.format("%.2f < %s < %.2f", lowerBound, name, upperBound);
		}
		return name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public double getLowerBound()
	{
		return lowerBound;
	}

	public void setLowerBound(double lowerBound)
	{
		this.lowerBound = lowerBound;
	}

	public double getUpperBound()
	{
		return upperBound;
	}

	public void setUpperBound(double upperBound)
	{
		this.upperBound = upperBound;
	}

	public double getRelativeRatio()
	{
		return relativeRatio;
	}

	public void setRelativeRatio(double relativeRatio)
	{
		this.relativeRatio = relativeRatio;
	}
}
