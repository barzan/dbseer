package dbseer.comp;

/**
 * Created by dyoon on 15. 4. 13..
 */
public class TransactionFetchThread extends Thread
{
	TransactionReader reader;

	public TransactionFetchThread(TransactionReader reader)
	{
		this.reader = reader;
	}

	@Override
	public void run()
	{
		reader.fetchTransaction();
	}
}
