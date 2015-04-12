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
