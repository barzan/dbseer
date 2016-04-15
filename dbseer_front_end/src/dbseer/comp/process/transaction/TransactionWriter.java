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

package dbseer.comp.process.transaction;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dong Young Yoon on 1/21/16.
 */
public class TransactionWriter
{
	private PrintWriter tpsWriter;
	private PrintWriter latencyWriter;
	private HashMap<Integer, PrintWriter> prctileLatencyWriter;
	private HashMap<Integer, ArrayList<Double>> latencyMap;
	private HashMap<Integer, PrintWriter> transactionSampleWriter;
	private HashMap<Integer, Integer> transactionSampleCountMap;

	public TransactionWriter(PrintWriter tpsWriter, PrintWriter latencyWriter, HashMap<Integer, PrintWriter> prctileLatencyWriter,
	                         HashMap<Integer, PrintWriter> transactionSampleWriter, HashMap<Integer, ArrayList<Double>> latencyMap)
	{
		this.tpsWriter = tpsWriter;
		this.latencyWriter = latencyWriter;
		this.prctileLatencyWriter = prctileLatencyWriter;
		this.latencyMap = latencyMap;
		this.transactionSampleWriter = transactionSampleWriter;
		this.transactionSampleCountMap = new HashMap<Integer, Integer>();
	}

	public PrintWriter getTpsWriter()
	{
		return tpsWriter;
	}

	public PrintWriter getLatencyWriter()
	{
		return latencyWriter;
	}

	public HashMap<Integer, PrintWriter> getPrctileLatencyWriter()
	{
		return prctileLatencyWriter;
	}

	public HashMap<Integer, ArrayList<Double>> getLatencyMap()
	{
		return latencyMap;
	}

	public HashMap<Integer, Integer> getTransactionSampleCountMap()
	{
		return transactionSampleCountMap;
	}

	public HashMap<Integer, PrintWriter> getTransactionSampleWriter()
	{
		return transactionSampleWriter;
	}
}
