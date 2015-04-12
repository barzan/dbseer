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
