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

/**
 * Created by dyoon on 9/16/15.
 */
public class IncrementalLog
{
	public static final int TYPE_STATEMENT = 1;
	public static final int TYPE_QUERY = 2;
	public static final int TYPE_TRANSACTION = 3;
	public static final int TYPE_SYSLOG = 4;

	private int type;
	private byte[] buf;
	private long offset;

	public IncrementalLog(int type, byte[] buf, long offset)
	{
		this.type = type;
		this.buf = buf;
		this.offset = offset;
	}

	public int getType()
	{
		return type;
	}

	public long getOffset()
	{
		return offset;
	}

	public byte[] getBuf()
	{
		return buf;
	}
}
