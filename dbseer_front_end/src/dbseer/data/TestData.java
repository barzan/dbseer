package dbseer.data;

/**
 * Created by dyoon on 2014. 5. 16..
 */
public class TestData
{
	private double[][] twoDimArray = new double[20][20];

	public TestData()
	{

	}

	public TestData(double[][] twoDimArray)
	{
		this.twoDimArray = twoDimArray;
	}

	public void SetElement(int row, int col, double val)
	{
		twoDimArray[row][col] = val;
	}

	public double GetElement(int row, int col)
	{
		return twoDimArray[row][col];
	}

}
