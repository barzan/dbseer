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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * The class to store and print data and info about transaction between one
 * client and server
 * 
 * @author Hongyu Wu
 * 
 */
public class TransactionData {
  public static final byte MYSQL_QUERY = 3;

  private MiddleServer middleServer;
  private SharedData sharedData;
  private int clientPortNum;
  private String userId;

  private long traxStart;
  private long traxEnd;
  private boolean inTrax;
  private boolean autoCommit;
  private boolean tempAutoCommit;
  private long queryStart;
  public boolean inQuery;

  private int txId;
  private int stId;
  private long queryId;

  private File txFile;
  private BufferedOutputStream txFileOutputStream;
  private File stFile;
  private BufferedOutputStream stFileOutputStream;
  private File qFile;
  private BufferedOutputStream qFileOutputStream;

  private ByteBuffer queryBuffer;
  private int bufferSize = 1024;

  public boolean endingTrax;
  public boolean isAlive;

  TransactionData(SharedData s, MiddleServer server) {
    sharedData = s;
    middleServer = server;
    clientPortNum = middleServer.getClientPort();
    userId = null;

    inTrax = false;
    inQuery = false;
    autoCommit = true;
    tempAutoCommit = true;
    endingTrax = false;
    isAlive = true;

    stId = 1;
  }

  public void processData(byte[] data, int len, long recTime) {

    if (!inQuery) {
      if (!inTrax) {
        traxStart = recTime;
        txId = sharedData.txId.incrementAndGet();

        if ((autoCommit && tempAutoCommit) || traxEnd(data, len)) {
          endingTrax = true;
        } else {
          inTrax = true;
        }

      } else {
        if (traxEnd(data, len)) {
          inTrax = false;
          endingTrax = true;
        }
      }

      queryStart = recTime;

      while (len - 5 + 1 > bufferSize)
      {
        bufferSize *= 2;
      }

      queryBuffer = ByteBuffer.allocate(bufferSize);
      queryBuffer.put((byte) ',');
      queryBuffer.put(data, 5, len - 5);

      inQuery = true;

    } else {
      while (len > queryBuffer.remaining()) {
        ByteBuffer tmp = queryBuffer;
        bufferSize *= 2;
        queryBuffer = ByteBuffer.allocate(bufferSize);
        tmp.limit(tmp.position());
        tmp.position(0);
        queryBuffer.put(tmp);
      }
      queryBuffer.put(data, 0, len);
    }
  }

  public void endTrax(long t) {

    traxEnd = t;
    String s = "," + clientPortNum + "," + userId + "," + (traxStart / 1000L)
        + "," +(traxEnd/1000L) + "," + (traxEnd - traxStart) + "," + queryId + "\n";

    sharedData.liveMonitor.endTransaction(txId, userId, traxStart/1000L, traxEnd/1000L, (traxEnd - traxStart));
    sharedData.allTransactions.put(txId, s.getBytes());
    try {
      if (txFileOutputStream != null)
      {
        txFileOutputStream.write(Integer.toString(txId).getBytes());
        txFileOutputStream.write(s.getBytes());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    stId = 1;
    endingTrax = false;
  }

  public void endQuery(long t) {
    queryId = sharedData.queryId.incrementAndGet();
    String s = txId + "," + stId + "," + queryId + "," + (queryStart/1000L)
        + "," + (t/1000L) + "," + (t - queryStart) + "\n";
    if (queryBuffer.remaining() < 2) {
      ByteBuffer tmp = queryBuffer;
      queryBuffer = ByteBuffer.allocate(bufferSize + 2);
      tmp.limit(tmp.position());
      tmp.position(0);
      queryBuffer.put(tmp);
    }

    byte[] originalArray = queryBuffer.array();
    byte[] queryArray = new byte[queryBuffer.position()-1];
    System.arraycopy(originalArray, 1, queryArray, 0, queryBuffer.position()-1);
    sharedData.liveMonitor.addQuery(txId, queryId, (queryStart / 1000L), (t / 1000L), (t - queryStart),
        new String(queryArray, Charset.forName("UTF-8")));
    queryBuffer.put((byte) 0);
    queryBuffer.put((byte) '\n');
    // sharedData.allStatementsInfo.add(s.getBytes());
    sharedData.allQueries
        .put(queryId, new QueryData(s.getBytes(), queryBuffer));

    try {
      if (stFileOutputStream != null)
      {
        stFileOutputStream.write(s.getBytes());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      if (qFileOutputStream != null)
      {
        qFileOutputStream.write(Long.toString(queryId).getBytes());
        qFileOutputStream.write(queryBuffer.array(), 0, queryBuffer.position());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    queryBuffer = null;
    inQuery = false;
    ++stId;

  }

  public void checkAutoCommit(byte[] data, int len) {
    if (len < 6 || len > 30)
      return;

    String s = new String(data, 5, len - 5);
    s = s.toLowerCase();
    s = s.replaceAll("\\s", "");
    if (s.contentEquals("setautocommit=0"))
      autoCommit = false;
    else if (s.contentEquals("setautocommit=1"))
      autoCommit = true;

    if (autoCommit) {
      if (s.contentEquals("begin") || s.contentEquals("starttransaction")) {
        tempAutoCommit = false;
      }
      if (s.contentEquals("commit") || s.contentEquals("rollback")) {
        tempAutoCommit = true;
      }
    }

  }

  private boolean traxEnd(byte[] data, int len) {
    if (len < 6 || len > 18)
      return false;
    String s = new String(data, 5, len - 5);

    s = s.toLowerCase();
    s = s.replaceAll("\\s", "");
    if (s.contentEquals("commit") || s.contentEquals("rollback"))
      return true;
    else
      return false;
  }

  public void flushToFile() {
    if (txFileOutputStream != null) {
      try {
        txFileOutputStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (stFileOutputStream != null) {
      try {
        stFileOutputStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (qFileOutputStream != null) {
      try {
        qFileOutputStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void openFileOutputStream() {

    txFile = new File(sharedData.getFilePathName() + "/Transactions/client-"
        + clientPortNum + "-t.txt");
    try {
      txFileOutputStream = new BufferedOutputStream(
          new FileOutputStream(txFile));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    stFile = new File(sharedData.getFilePathName() + "/Transactions/client-"
        + clientPortNum + "-s.txt");
    try {
      stFileOutputStream = new BufferedOutputStream(
          new FileOutputStream(stFile));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    qFile = new File(sharedData.getFilePathName() + "/Transactions/client-"
        + clientPortNum + "-q.txt");
    try {
      qFileOutputStream = new BufferedOutputStream(new FileOutputStream(qFile));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

  public void closeFileOutputStream() {
    if (txFileOutputStream != null) {
      try {
        txFileOutputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      txFileOutputStream = null;
    }
    if (stFileOutputStream != null) {
      try {
        stFileOutputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      stFileOutputStream = null;
    }
    if (qFileOutputStream != null) {
      try {
        qFileOutputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      qFileOutputStream = null;
    }
  }

}
