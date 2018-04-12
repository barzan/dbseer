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

package middleware;

import com.google.common.collect.EvictingQueue;

/**
 * Created by dyoon on 5/20/15.
 */
public class LiveTransactionStatistic
{
	private final int MAX_EXAMPLE_LIMIT = 30;
	public double totalTransactionCounts = 0;
	public double currentAverageLatency = 0;
	public double currentTransactionCounts = 0;
	public EvictingQueue<String> examples;

	public LiveTransactionStatistic()
	{
		examples = EvictingQueue.create(MAX_EXAMPLE_LIMIT);
	}
}
