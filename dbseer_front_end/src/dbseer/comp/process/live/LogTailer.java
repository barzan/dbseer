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

package dbseer.comp.process.live;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Dong Young Yoon on 4/4/16.
 */
public class LogTailer implements Runnable

{
	private static final int DEFAULT_DELAY_MILLIS = 1000;

	private static final String RAF_MODE = "r";

	private static final int DEFAULT_BUFSIZE = 8192;

	/**
	 * Buffer on top of RandomAccessFile.
	 */
	private final byte inbuf[];

	/**
	 * The file which will be tailed.
	 */
	private final File file;

	/**
	 * The amount of time to wait for the file to be updated.
	 */
	private final long delayMillis;

	/**
	 * The listener to notify of events when tailing.
	 */
	private final LiveLogTailer listener;
//	private final LogTailerListener listener;

	private boolean resetFilePositionIfOverwrittenWithTheSameLength;

	/**
	 * The tailer will run as long as this value is true.
	 */
	private volatile boolean run = true;

	private volatile long startOffset = 0;

	public LogTailer(File file, LiveLogTailer listener, long delayMillis, long startOffset)
	{
		this.file = file;
		this.delayMillis = delayMillis;

		this.inbuf = new byte[DEFAULT_BUFSIZE];

		// Save and prepare the listener
		this.listener = listener;
		this.startOffset = startOffset;
	}

	public LogTailer(File file, LiveLogTailer listener, long delayMillis, long startOffset, boolean resetFilePositionIfOverwrittenWithTheSameLength)
	{
		this.file = file;
		this.delayMillis = delayMillis;

		this.inbuf = new byte[DEFAULT_BUFSIZE];

		// Save and prepare the listener
		this.listener = listener;
		this.startOffset = startOffset;
		this.resetFilePositionIfOverwrittenWithTheSameLength = resetFilePositionIfOverwrittenWithTheSameLength;
	}

	@Override
	public void run()
	{
		RandomAccessFile reader = null;
		try {
			long last = 0; // The last time the file was checked for changes
			long position = 0; // position within the file
			// Open the file
			while (run && reader == null) {
				try {
					reader = new RandomAccessFile(file, RAF_MODE);
				} catch (FileNotFoundException e) {
					listener.fileNotFound();
				}

				if (reader == null) {
					try {
						Thread.sleep(delayMillis);
					} catch (InterruptedException e) {
					}
				} else {
					// The current position in the file
//					position = (startOffset > file.length()) ? file.length() : startOffset;
					if (startOffset == -1)
					{
						position = file.length();
					}
					else
					{
						position = startOffset;
					}
					last = System.currentTimeMillis();
					reader.seek(position);
				}
			}

			while (run) {

				boolean newer = FileUtils.isFileNewer(file, last); // IO-279, must be done first

				// Check the file length to see if it was rotated
				long length = file.length();

				if (length < position) {

					// File was rotated
					listener.fileRotated();

					// Reopen the reader after rotation
					try {
						// Ensure that the old file is closed iff we re-open it successfully
						RandomAccessFile save = reader;
						reader = new RandomAccessFile(file, RAF_MODE);
						position = 0;
						// close old file explicitly rather than relying on GC picking up previous RAF
						IOUtils.closeQuietly(save);
					} catch (FileNotFoundException e) {
						// in this case we continue to use the previous reader and position values
						listener.fileNotFound();
					}
					continue;
				} else {

					// File was not rotated

					// See if the file needs to be read again
					if (length > position) {

						// The file has more content than it did last time
						position = readLines(reader);
						last = System.currentTimeMillis();

					} else if (newer) {

                        /*
                         * This can happen if the file is truncated or overwritten with the exact same length of
                         * information. In cases like this, the file position needs to be reset
                         */
						if (resetFilePositionIfOverwrittenWithTheSameLength)
						{
							position = 0;
							reader.seek(position); // cannot be null here

							// Now we can read new lines
							position = readLines(reader);
							last = System.currentTimeMillis();
						}
					} else {
						Thread.sleep(DEFAULT_DELAY_MILLIS);
					}
				}
			}

		} catch (Exception e) {

			listener.handle(e);

		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	/**
	 * Read new lines.
	 *
	 * @param reader The file to read
	 * @return The new position after the lines have been read
	 * @throws java.io.IOException if an I/O error occurs.
	 */
	private long readLines(RandomAccessFile reader) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		long pos = reader.getFilePointer();
		long rePos = pos; // position to re-read

		int num;
		boolean seenCR = false;
		while (run && ((num = reader.read(inbuf)) != -1)) {
			for (int i = 0; i < num; i++) {
				byte ch = inbuf[i];
				switch (ch) {
					case '\n':
						seenCR = false; // swallow CR before LF
						listener.handle(sb.toString(), pos + i + 1);
						sb.setLength(0);
						rePos = pos + i + 1;
						break;
					case '\r':
						if (seenCR) {
							sb.append('\r');
						}
						seenCR = true;
						break;
					default:
						if (seenCR) {
							seenCR = false; // swallow final CR
							listener.handle(sb.toString(), pos + i + 1);
							sb.setLength(0);
							rePos = pos + i + 1;
						}
						sb.append((char) ch); // add character, not its ascii value
				}
			}

			pos = reader.getFilePointer();
		}

		reader.seek(rePos); // Ensure we can re-read if necessary
		return rePos;
	}
}
