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

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The thread to listen to middle port and construct connection to both clients
 * and server
 * 
 * @author Hongyu Wu
 * 
 */
public class NewServerSocket extends Thread {

  private SharedData sharedData;
  private ServerSocketChannel serverSocketChannel;
  private ServerSocketChannel adminServerSocketChannel;
  private Selector selector;
  private Iterator<SelectionKey> keyIterator;
  private File dir;
  private int numWorkers;
  private NewWorker[] workers;
  private byte[] data;
  private ByteBuffer buffer;
  private boolean endingMonitoring;
  private boolean sendingFiles;
  private boolean monitoring;
  private boolean dstatDeployed;
  private boolean failDeployDstat;
  private boolean configSetenv;
  private boolean sendLog = false;
  private boolean isIncrementalLogging = false;

  private String mysql_user;
  private String mysql_pass;
  private String mysql_host;
  private String mysql_port;

//  private Thread liveAggregateProcessThread = null;
  private Thread incrementalLogThread = null;
  private IncrementalLogSender incrementalLogSender = null;

  private LogTailer sysLogTailer = null;
  private LogTailer sLogTailer = null;
  private LogTailer qLogTailer = null;
  private LogTailer tLogTailer = null;

  private Thread sysLogTailerThread = null;
  private Thread sLogTailerThread = null;
  private Thread qLogTailerThread = null;
  private Thread tLogTailerThread = null;

  private Map<String, byte[]> userInfo;

  private String zipFileName = "LogFiles.zip";

  private byte[] fileBuffer;

  private MiddleSocketChannel curUser;

  private ArrayList<MiddleSocketChannel> userList;

  private Process dstat;

  private Process ntpdate;

  private Process stopRemoteDstat;

  private BlockingQueue<IncrementalLog> incrementalLogQueue;

  NewServerSocket(SharedData s) {
    sharedData = s;
    try {
      serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.socket().bind(
          new InetSocketAddress(s.getMiddlePortNum()));
      serverSocketChannel.configureBlocking(false);

    } catch (IOException e) {
      System.out.println("Error: cannot bind to port " + s.getMiddlePortNum());
      e.printStackTrace();
    }

    try {
      adminServerSocketChannel = ServerSocketChannel.open();
      adminServerSocketChannel.socket().bind(
          new InetSocketAddress(s.getAdminPortNum()));
      adminServerSocketChannel.configureBlocking(false);
    } catch (IOException e) {
      System.out.println("Error: cannot bind to port " + s.getAdminPortNum());
      e.printStackTrace();
    }

    try {
      selector = Selector.open();
      serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
      adminServerSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    } catch (IOException e) {
      e.printStackTrace();
    }

    keyIterator = null;

    dir = new File(sharedData.getFilePathName() + File.separator
        + "Transactions");
    if (!dir.exists()) {
      dir.mkdirs();
    } else {
      for (File f : dir.listFiles()) {
        if (!f.delete()) {
          // TODO
        }
      }
    }

    numWorkers = sharedData.getNumWorkers();
    workers = new NewWorker[numWorkers];
    for (int i = 0; i < numWorkers; ++i) {
      Selector tmpS = null;
      try {
        tmpS = Selector.open();
      } catch (IOException e) {
        e.printStackTrace();
      }

      workers[i] = new NewWorker(sharedData, tmpS);
      workers[i].start();
    }

    data = new byte[sharedData.getMaxSize()];
    buffer = ByteBuffer.wrap(data);
    endingMonitoring = false;
    sendingFiles = false;
    monitoring = false;
    dstatDeployed = false;
    failDeployDstat = false;
    configSetenv = false;

    mysql_user = null;
    mysql_pass = null;
    mysql_host = null;
    mysql_port = null;

    sharedData.allTransactionData = new ArrayList<TransactionData>();
    sharedData.allTransactions = new ConcurrentSkipListMap<Integer, byte[]>();
    // sharedData.allStatementsInfo = new ConcurrentLinkedQueue<byte[]>();
    sharedData.allQueries = new ConcurrentSkipListMap<Long, QueryData>();

    userInfo = Encrypt.getUsrMap(sharedData.getUserInfoFilePath());

    fileBuffer = new byte[1024];

    curUser = null;

    userList = new ArrayList<MiddleSocketChannel>();

    dstat = null;

    ntpdate = null;

    stopRemoteDstat = null;

    incrementalLogQueue = new ArrayBlockingQueue<IncrementalLog>(64 * 1024);
  }

  public void run() {
    int count = 0;
    while (!sharedData.isEndOfProgram()) {

      try {
        if (monitoring) {
          selector.select(10);
        } else {
          selector.select();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      keyIterator = selector.selectedKeys().iterator();
      while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();
        keyIterator.remove();

        if (key.isAcceptable()) {
          if (key.channel() == serverSocketChannel) {
            SocketChannel socketChannel = null;
            try {
              socketChannel = serverSocketChannel.accept();
            } catch (IOException e) {
              e.printStackTrace();
            }

            if (socketChannel != null) {

              MiddleClient middleClient = new MiddleClient(
                  sharedData.getServerIpAddr(), sharedData.getServerPortNum());
              middleClient.startClient();

              MiddleServer middleServer = new MiddleServer(socketChannel);
              TransactionData transactionData = new TransactionData(sharedData,
                  middleServer);
              middleServer.startServer(transactionData);

              int len = 0;
              buffer.clear();
              len = middleClient.getInput(buffer);
              middleServer.sendOutput(buffer, len);

              buffer.clear();
              len = middleServer.getInput(buffer);
              transactionData.setUserId(getUserId(data));
              middleClient.sendOutput(buffer, len);

              middleServer.setNonBlocking();
              middleClient.setNonBlocking();

              if (sharedData.isOutputToFile()) {
                transactionData.openFileOutputStream();
              }

              sharedData.allTransactionData.add(transactionData);

              workers[count % numWorkers].socketMap.put(
                  middleServer.socketChannel, middleServer);
              workers[count % numWorkers].socketMap.put(
                  middleClient.socketChannel, middleClient);

              middleServer.register(workers[count % numWorkers].selector,
                  middleClient);

              middleClient.register(workers[count % numWorkers].selector,
                  middleServer);

              ++count;
            }
          } else if (key.channel() == adminServerSocketChannel) {
            SocketChannel sock = null;
            try {
              sock = adminServerSocketChannel.accept();
            } catch (IOException e) {
              e.printStackTrace();
            }
            if (sock != null) {
              try {
                sock.configureBlocking(true);
              } catch (IOException e) {
                e.printStackTrace();
              }
              MiddleSocketChannel middleSocketChannel = new MiddleSocketChannel(
                  sock);
              middleSocketChannel.setNonBlocking();

              middleSocketChannel.register(selector, middleSocketChannel);

            }
          }
        } else if (key.isReadable()) {
          MiddleSocketChannel middleSocketChannel = (MiddleSocketChannel) key
              .attachment();

          buffer.clear();
          int len = middleSocketChannel.getInput(buffer);
          if (len == -1) {
            middleSocketChannel.cancelKey();
            continue;
          }
          buffer.position(0);

          int packetID = 0;
          long packetLength = -1;
          boolean isValidPacket = true;

          try {
            packetID = buffer.getInt();
            packetLength = buffer.getLong();
          } catch (BufferUnderflowException e) {
            buffer.clear();
            buffer.putInt(102);
            String response = "Invalid packet header";
            buffer.putLong(response.length());
            buffer.put(response.getBytes());
            isValidPacket = false;
            middleSocketChannel.sendOutput(buffer, buffer.position());
          }

          if (isValidPacket) {
            if (packetID == 100)
            {
              if (userList.contains(middleSocketChannel))
              {
                buffer.clear();
                buffer.putInt(102);
                String response = "You have already logged in";
                buffer.putLong(response.length());
                buffer.put(response.getBytes());
                middleSocketChannel.sendOutput(buffer, buffer.position());
              }
              else if (packetLength <= 0)
              {
                buffer.clear();
                buffer.putInt(102);
                String response = "Invalid packet length";
                buffer.putLong(response.length());
                buffer.put(response.getBytes());
                middleSocketChannel.sendOutput(buffer, buffer.position());
              }
              else
              {
                String userID = null;
                byte[] password = new byte[Encrypt.MAX_LENGTH];
                byte[] packet = new byte[(int) packetLength];
                buffer.get(packet);
                userID = parseLogInPacket(packet, password);
                if (userInfo.get(userID) != null
                        && Arrays.equals(((byte[]) userInfo.get(userID)),
                        Encrypt.encrypt(password)))
                {
                  buffer.clear();
                  buffer.putInt(101);
                  buffer.putLong(0);
                  middleSocketChannel.sendOutput(buffer, buffer.position());
                  userList.add(middleSocketChannel);
                }
                else
                {
                  buffer.clear();
                  buffer.putInt(102);
                  String response = "Invalid User ID or password";
                  buffer.putLong(response.length());
                  buffer.put(response.getBytes());
                  middleSocketChannel.sendOutput(buffer, buffer.position());
                }
              }
            } else if (packetID == 103) {
              stopIncrementalLogging();
            } else if (packetID == 200) {
              if (userList.contains(middleSocketChannel)) {
                if (sharedData.isOutputToFile() || endingMonitoring
                    || sendingFiles) {
                  String response = "Current monitoring not finished";
                  buffer.clear();
                  buffer.putInt(202);
                  buffer.putLong(response.length());
                  buffer.put(response.getBytes());
                  middleSocketChannel.sendOutput(buffer, buffer.position());
                } else {
                  startMonitoring();
                  buffer.clear();
                  buffer.putInt(201);
                  buffer.putLong(0);
                  middleSocketChannel.sendOutput(buffer, buffer.position());
                  curUser = middleSocketChannel;
                }
              } else {
                buffer.clear();
                buffer.putInt(102);
                String response = "You have not been registered";
                buffer.putLong(response.length());
                buffer.put(response.getBytes());
                middleSocketChannel.sendOutput(buffer, buffer.position());
              }

            } else if (packetID == 300) {
              if (userList.contains(middleSocketChannel)) {
                if (!sharedData.isOutputToFile()) {
                  String response = "No monitoring running";
                  buffer.clear();
                  buffer.putInt(302);
                  buffer.putLong(response.length());
                  buffer.put(response.getBytes());
                  middleSocketChannel.sendOutput(buffer, buffer.position());
                  // DY : I think this use case is not right at the moment where
                  // monitoring is only stoppable by a user who started it.
//                } else if (middleSocketChannel != curUser) {
//                  String response = "Monitoring running by other user";
//                  buffer.clear();
//                  buffer.putInt(302);
//                  buffer.putLong(response.length());
//                  buffer.put(response.getBytes());
//                  middleSocketChannel.sendOutput(buffer, buffer.position());
                } else if (endingMonitoring) {
                  String response = "Writing log files, please wait";
                  buffer.clear();
                  buffer.putInt(302);
                  buffer.putLong(response.length());
                  buffer.put(response.getBytes());
                  middleSocketChannel.sendOutput(buffer, buffer.position());
                } else {
                  sendLog = true;
                  curUser = middleSocketChannel;
                  stopMonitoring();
                }
              } else {
                buffer.clear();
                buffer.putInt(102);
                String response = "You have not been registered";
                buffer.putLong(response.length());
                buffer.put(response.getBytes());
                middleSocketChannel.sendOutput(buffer, buffer.position());
              }
            } else if (packetID == 303) {
              if (userList.contains(middleSocketChannel)) {
                if (!sharedData.isOutputToFile()) {
                  String response = "No monitoring running";
                  buffer.clear();
                  buffer.putInt(302);
                  buffer.putLong(response.length());
                  buffer.put(response.getBytes());
                  middleSocketChannel.sendOutput(buffer, buffer.position());
//                } else if (middleSocketChannel != curUser) {
//                  String response = "Monitoring running by other user";
//                  buffer.clear();
//                  buffer.putInt(302);
//                  buffer.putLong(response.length());
//                  buffer.put(response.getBytes());
//                  middleSocketChannel.sendOutput(buffer, buffer.position());
                } else if (endingMonitoring) {
                  String response = "Writing log files, please wait";
                  buffer.clear();
                  buffer.putInt(302);
                  buffer.putLong(response.length());
                  buffer.put(response.getBytes());
                  middleSocketChannel.sendOutput(buffer, buffer.position());
                } else {
                  sendLog = false;
                  stopMonitoring();
                }
              } else {
                buffer.clear();
                buffer.putInt(102);
                String response = "You have not been registered";
                buffer.putLong(response.length());
                buffer.put(response.getBytes());
                middleSocketChannel.sendOutput(buffer, buffer.position());
              }

            } else if (packetID == 400) {
              if (userList.contains(middleSocketChannel)) {
                // when the GUI reconnects and the middleware is still monitoring...
                if (monitoring) {

                  // start new logging threads and resume monitoring
                  stopIncrementalLogging();
                  startIncrementalLogging(true);

                  buffer.clear();
                  buffer.putInt(402);
                  buffer.putLong(0);
                  middleSocketChannel.sendOutput(buffer, buffer.position());
                } else {
                  buffer.clear();
                  buffer.putInt(401);
                  buffer.putLong(0);
                  middleSocketChannel.sendOutput(buffer, buffer.position());
                }
              } else {
                buffer.clear();
                buffer.putInt(102);
                String response = "You have not been registered";
                buffer.putLong(response.length());
                buffer.put(response.getBytes());
                middleSocketChannel.sendOutput(buffer, buffer.position());
              }

            } else if (packetID == 500) {
              if (!sharedData.isLiveMonitoring()) {
                buffer.clear();
                buffer.putInt(501);
                middleSocketChannel.sendOutput(buffer, buffer.position());
              } else {
                buffer.clear();
                buffer.putInt(502);
								LiveAggregateGlobal globalAggregate = sharedData.liveMonitor.globalAggregate;
								int numTransactionType = globalAggregate.getNumTransactionType();
								buffer.putInt(numTransactionType);
                buffer.putDouble(globalAggregate.totalTransactionCount);
								for (int i = 0; i < numTransactionType; ++i)
								{
									buffer.putDouble(globalAggregate.transactionStatistics.get(i).currentTransactionCounts); // current TPS
									buffer.putDouble(globalAggregate.transactionStatistics.get(i).currentAverageLatency); // current average latency.
									buffer.putDouble(globalAggregate.transactionStatistics.get(i).totalTransactionCounts); // total transaction count
								}
                middleSocketChannel.sendOutput(buffer, buffer.position());
              }
            } else if (packetID == 600) {
              int type = buffer.getInt();
              int index = buffer.getInt();
              String[] samples = sharedData.liveMonitor.getTransactionSamples(type);
              if (samples == null) {
                buffer.clear();
                buffer.putInt(601);
                middleSocketChannel.sendOutput(buffer, buffer.position());
              } else if (samples.length < index + 1) {
                buffer.clear();
                buffer.putInt(601);
                middleSocketChannel.sendOutput(buffer, buffer.position());
              } else {
                String sample = samples[index];
                buffer.clear();
                buffer.putInt(602);
                buffer.putLong(sample.length());
                buffer.put(sample.getBytes());
                middleSocketChannel.sendOutput(buffer, buffer.position());
              }
            } else if (packetID == 700) {
              int type = buffer.getInt();
                sharedData.liveMonitor.removeTransactionType(type);
              buffer.clear();
              buffer.putInt(701);
              buffer.putInt(sharedData.liveMonitor.globalAggregate.getNumTransactionType());
              middleSocketChannel.sendOutput(buffer, buffer.position());
            } else {
              buffer.clear();
              buffer.putInt(102);
              String response = "Invalid packet ID";
              buffer.putLong(response.length());
              buffer.put(response.getBytes());
              middleSocketChannel.sendOutput(buffer, buffer.position());
            }
          }
        }
      }

      if (!sharedData.allTransactions.isEmpty()
          || !sharedData.allQueries.isEmpty()) {
        int c = sharedData.allQueries.size();
        while (c > 0) {
          printQueries();
          --c;
        }
        // c = sharedData.allStatementsInfo.size();
        // while (c > 0) {
        // printStatementsInfo();
        // --c;
        // }
        c = sharedData.allTransactions.size();
        while (c > 0) {
          printTransactions();
          --c;
        }
      } else if (endingMonitoring) {
        try {
          sharedData.tAllLogFileOutputStream.flush();
          sharedData.tAllLogFileOutputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          sharedData.sAllLogFileOutputStream.flush();
          sharedData.sAllLogFileOutputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          sharedData.qAllLogFileOutputStream.flush();
          sharedData.qAllLogFileOutputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

        if (curUser != null && sendLog) {

//          System.out.println("ready to compress log files");

          if (stopRemoteDstat != null) {
            Interrupter interrupter = new Interrupter(Thread.currentThread());
            interrupter.start();
            try {
              stopRemoteDstat.waitFor();
              interrupter.setOuterThreadWaiting(false);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            stopRemoteDstat = null;
          }
            buffer.clear();
            buffer.putInt(301);
            buffer.putLong(0);
            curUser.sendOutput(buffer, buffer.position());

//          if (zipAllFiles()) {
//
//            System.out
//                .println("finish compressing files, ready to send zip file");
//
//            File zipFile = new File(zipFileName);
//
//            FileInputStream fis = null;
//            try {
//              fis = new FileInputStream(zipFile);
//            } catch (FileNotFoundException e) {
//              e.printStackTrace();
//            }
//
//            FileChannel fc = fis.getChannel();
//
//            buffer.clear();
//            buffer.putInt(301);
//            buffer.putLong(zipFile.length());
//            curUser.sendOutput(buffer, buffer.position());
//            long position = 0;
//            long remaining = zipFile.length();
//            long len = 0;
//            while (remaining > 0) {
//              try {
//               len = fc.transferTo(position, 1024, curUser.socketChannel);
//              } catch (IOException e) {
//                e.printStackTrace();
//              }
//              position += len;
//              remaining -= len;
//              len = 0;
//            }
//
//            System.out.println("finish sending zip file");
//
//          } else {
//            String response = "fail to compress log files";
//            buffer.clear();
//            buffer.putInt(302);
//            buffer.putLong(response.length());
//            buffer.put(response.getBytes());
//            curUser.sendOutput(buffer, buffer.position());
//          }
          curUser = null;
        }

        endingMonitoring = false;
        monitoring = false;
        if (System.getProperty("user.name").contentEquals("root")) {
          String[] cmd = { "/bin/bash", "shell/chmod" };
          try {
            Runtime.getRuntime().exec(cmd);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

      }

    }

  }

  private boolean zipAllFiles() {

    File zipFile = new File(zipFileName);
    int index = 0;
    while (zipFile.exists()) {
      if (!zipFile.delete()) {
        ++index;
        zipFileName = "LogFiles_" + index + ".zip";
        zipFile = new File(zipFileName);
      }
    }

    try {

      FileOutputStream fos = new FileOutputStream(zipFileName);

      ZipOutputStream zos = new ZipOutputStream(fos);

      File[] files = dir.listFiles();

      for (int i = 0; i < files.length; i++) {

        // only sends the dstat log file.
        if (files[i].getName().contains("log_exp_1"))
        {
          FileInputStream fis = new FileInputStream(files[i]);

          // begin writing a new ZIP entry, positions the stream to the start of
          // the entry data
          zos.putNextEntry(new ZipEntry(files[i].getName()));

          int length;

          while ((length = fis.read(fileBuffer)) > 0)
          {
            zos.write(fileBuffer, 0, length);
          }

          zos.closeEntry();

          // close the InputStream
          fis.close();
        }
      }

      // close the ZipOutputStream
      zos.close();

    } catch (IOException ioe) {
      return false;
    }

    return true;

  }

  private String parseLogInPacket(byte[] packet, byte[] password) {
    int i = 0;
    StringBuilder s = new StringBuilder();
    for (; i < packet.length; ++i) {
      if (packet[i] == '=' && i + 1 < packet.length) {
        ++i;
        do {
          s.append((char) packet[i]);
          ++i;
        } while (i < packet.length && packet[i] != ' ');
        break;
      }
    }
    for (; i < packet.length; ++i) {
      if (packet[i] == '=' && i + 1 < packet.length) {
        ++i;
        int j = 0;
        do {
          password[j] = packet[i];
          ++i;
          ++j;
        } while (i < packet.length && j < password.length);
        break;
      }
    }

    return s.toString();
  }

  private String getUserId(byte[] b) {
    int i = 36;
    StringBuilder stringBuilder = new StringBuilder();
    while (b[i] > (byte) 32 && b[i] < (byte) 127 && i < b.length) {
      stringBuilder.append((char) b[i]);
      ++i;
    }
    return stringBuilder.toString();
  }

  private void printTransactions() {
    Entry<Integer, byte[]> tmpE = sharedData.allTransactions.pollFirstEntry();
    Integer TxID = tmpE.getKey();
    byte[] tmpB = tmpE.getValue();

    try {
      byte[] id = TxID.toString().getBytes();
      byte[] combined = new byte[id.length + tmpB.length];
      System.arraycopy(id, 0, combined, 0, id.length);
      System.arraycopy(tmpB, 0, combined, id.length, tmpB.length);

      sharedData.tAllLogFileOutputStream.write(combined);
//      sharedData.tAllLogFileOutputStream.write(TxID.toString().getBytes());
//      sharedData.tAllLogFileOutputStream.write(tmpB);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  //
  // private void printStatementsInfo() {
  // byte[] tmpB = sharedData.allStatementsInfo.poll();
  //
  // try {
  // sharedData.sAllLogFileOutputStream.write(tmpB);
  // } catch (IOException e) {
  // e.printStackTrace();
  // }
  //
  // }

  private void printQueries() {
    Entry<Long, QueryData> tmpE = sharedData.allQueries.pollFirstEntry();
    Long qId = tmpE.getKey();
    QueryData tmp = tmpE.getValue();

    try {
      sharedData.sAllLogFileOutputStream.write(tmp.statementInfo);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      byte[] id = qId.toString().getBytes();
      byte[] combined = new byte[id.length + tmp.query.position()];
      System.arraycopy(id, 0, combined, 0, id.length);
      System.arraycopy(tmp.query.array(), 0, combined, id.length, tmp.query.position());

      sharedData.qAllLogFileOutputStream.write(combined);
//      sharedData.qAllLogFileOutputStream.write(qId.toString().getBytes());
//      sharedData.qAllLogFileOutputStream.write(tmp.query.array(), 0,
//          tmp.query.position());
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void startMonitoring() {
    for (File f : dir.listFiles()) {
      if (!f.delete()) {
        // TODO
      }
    }

    if (!zipFileName.contentEquals("LogFiles.zip")) {
      zipFileName = "LogFiles.zip";
    }
    File oldZipFile = new File(zipFileName);
    if (oldZipFile.exists()) {
      oldZipFile.delete();
    }

    for (int i = 0; i < sharedData.allTransactionData.size();) {
      TransactionData tmp = sharedData.allTransactionData.get(i);
      if (tmp.isAlive) {
        tmp.openFileOutputStream();
        ++i;
      } else {
        sharedData.allTransactionData.remove(i);
      }
    }

    try {
      sharedData.tAllLogFileOutputStream = new BufferedOutputStream(
          new FileOutputStream(new File(sharedData.getFilePathName()
              + "/Transactions/allLogs-t.txt")));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    try {
      sharedData.sAllLogFileOutputStream = new BufferedOutputStream(
          new FileOutputStream(new File(sharedData.getFilePathName()
              + "/Transactions/allLogs-s.txt")));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    try {
      sharedData.qAllLogFileOutputStream = new BufferedOutputStream(
          new FileOutputStream(new File(sharedData.getFilePathName()
              + "/Transactions/allLogs-q.txt")));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    sharedData.txId.set(0);
    sharedData.queryId.set(0);

    monitoring = true;
    selector.wakeup();

    startDstat();
    sharedData.setOutputToFile(true);

    sharedData.liveMonitor = new LiveMonitor();
//    liveAggregateProcessThread = new Thread(new LiveAggregateProcessor(sharedData.liveMonitor.getAggregateMap(),
//        System.currentTimeMillis() / 1000L, sharedData));
//    liveAggregateProcessThread = new Thread(new LiveAggregateProcessor(sharedData.liveMonitor.getAggregateMap(),
//        System.currentTimeMillis() / 1000L, sharedData.liveMonitor.globalAggregate));
//    liveAggregateProcessThread.start();

    // start incremental logging.
    startIncrementalLogging(false);

    sharedData.setIsLiveMonitoring(true);

    System.out.println("start monitoring");

  }

  public void stopMonitoring() {

    if (sharedData.isRemoteServer()) {
      if (dstatDeployed && !failDeployDstat) {
        String[] cmd = { "/bin/bash", "shell/stop_remote_monitor.sh",
            sharedData.remoteServerUser, sharedData.getServerIpAddr() };
        try {
          stopRemoteDstat = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    } else {
      File monitorPid = new File("rs-sysmon2/monitor.pid");
      String pid = null;

      if (monitorPid.exists()) {
        try {
          BufferedReader br = new BufferedReader(new InputStreamReader(
              new FileInputStream(monitorPid)));
          pid = br.readLine();
          br.close();
        } catch (FileNotFoundException e1) {
          e1.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }

        String[] cmd = { "pkill", "-15", "-P", pid };
        try {
          Process p = Runtime.getRuntime().exec(cmd);
          p.waitFor();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        monitorPid.delete();
      }
    }

    sharedData.setOutputToFile(false);
    dstat = null;
    endingMonitoring = true;

    if (ntpdate != null) {
      ntpdate.destroy();
    }

    for (int i = 0; i < sharedData.allTransactionData.size();) {
      TransactionData tmp = sharedData.allTransactionData.get(i);
      if (tmp.isAlive) {
        tmp.closeFileOutputStream();
        ++i;
      } else {
        sharedData.allTransactionData.remove(i);
      }
    }

    stopIncrementalLogging();

    sharedData.setIsLiveMonitoring(false);

    System.out.println("stop monitoring");

  }

  private void startDstat() {
    if (!sharedData.isRemoteServer()) {
      String[] cmd = { "/bin/bash", "./monitor.sh" };
      ProcessBuilder pb = new ProcessBuilder(cmd);
      pb.directory(new File("rs-sysmon2"));
      try {
        dstat = pb.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      if (sharedData.remoteServerUser.length() != 0) {
        if (System.getProperty("user.name").contentEquals("root")) {
          String[] cmd = { "/bin/bash", "shell/sync_time.sh",
              sharedData.getServerIpAddr() };
          try {
            ntpdate = Runtime.getRuntime().exec(cmd);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (!dstatDeployed) {
          configureSetenv();
          String[] cmd = { "/bin/bash", "shell/deploy_dstat.sh",
              sharedData.remoteServerUser, sharedData.getServerIpAddr() };
          Process deployDstat = null;
          try {
            deployDstat = Runtime.getRuntime().exec(cmd);
          } catch (IOException e) {
            e.printStackTrace();
          }
          dstatDeployed = true;
          if (deployDstat != null) {
            Interrupter interrupter = new Interrupter(Thread.currentThread());
            interrupter.start();
            try {
              deployDstat.waitFor();
              interrupter.setOuterThreadWaiting(false);
            } catch (InterruptedException e) {
              failDeployDstat = true;
            }
          }
          if (configSetenv) {
            clearSetenv();
          }
        }
        if (!failDeployDstat) {
          String[] cmd = { "/bin/bash", "shell/monitor_remote_server.sh",
              sharedData.remoteServerUser, sharedData.getServerIpAddr() };
          try {
            Runtime.getRuntime().exec(cmd);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    // if (dstat != null) {
    // BufferedReader b = new BufferedReader(new InputStreamReader(
    // dstat.getInputStream()));
    // String line = "";
    //
    // System.out.println("--------------");
    // try {
    // while ((line = b.readLine()) != null) {
    // System.out.println(line);
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // System.out.println("--------------");
    //
    // try {
    // b.close();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
  }

  private void configureSetenv() {
    BufferedReader br = null;
    ProcessBuilder pb = new ProcessBuilder("/bin/bash",
        "shell/config_setenv.sh");
    Map<String, String> env = pb.environment();
    try {
      br = new BufferedReader(new FileReader(sharedData.getUserInfoFilePath()));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    String line;
    try {
      while ((line = br.readLine()) != null) {
        String temp = line.replaceAll("\\s", "");
        if (temp.contains("mysql_user=")) {
          mysql_user = temp.substring(temp.indexOf('=') + 1);
          env.put("mMYSQL_USER", mysql_user);
          configSetenv = true;
        } else if (temp.contains("mysql_pass=")) {
          mysql_pass = temp.substring(temp.indexOf('=') + 1);
          env.put("mMYSQL_PASS", mysql_pass);
          configSetenv = true;
        } else if (temp.contains("mysql_host=")) {
          mysql_host = temp.substring(temp.indexOf('=') + 1);
          env.put("mMYSQL_HOST", mysql_host);
          configSetenv = true;
        } else if (temp.contains("mysql_port=")) {
          mysql_port = temp.substring(temp.indexOf('=') + 1);
          env.put("mMYSQL_PORT", mysql_port);
          configSetenv = true;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (configSetenv) {
      Process p = null;
      try {
        p = pb.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (p != null) {
        try {
          p.waitFor();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void clearSetenv() {
    ProcessBuilder pb = new ProcessBuilder("/bin/bash", "shell/clear_setenv.sh");
    Map<String, String> env = pb.environment();
    if (mysql_user != null) {
      env.put("mMYSQL_USER", mysql_user);
    }
    if (mysql_pass != null) {
      env.put("mMYSQL_PASS", mysql_pass);
    }
    if (mysql_host != null) {
      env.put("mMYSQL_HOST", mysql_host);
    }
    if (mysql_port != null) {
      env.put("mMYSQL_PORT", mysql_port);
    }
    Process p = null;
    try {
      p = pb.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (p != null) {
      try {
        p.waitFor();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void startIncrementalLogging(boolean resume)
  {
    if (isIncrementalLogging)
    {
      return;
    }
    // start dstat log tailer
    File[] files = dir.listFiles();
    File dstatFile = null;
    for (File file : files)
    {
      // use the first file found for now
      if (file.getName().contains("log_exp"))
      {
        dstatFile = file;
        break;
      }
    }

    if (dstatFile == null)
    {
      dstatFile = new File(dir + File.separator + "log_exp_1.csv");
    }

    if (dstatFile != null)
    {
      LogTailerListener tailerListener = new LogTailerListener(IncrementalLog.TYPE_SYSLOG, incrementalLogQueue, resume);
      sysLogTailer = new LogTailer(dstatFile, tailerListener, 1000, sharedData.sysStartOffset); // delay is 1s
      sysLogTailerThread = new Thread(sysLogTailer);
      sysLogTailerThread.start();
    }

    File qFile = new File(sharedData.getFilePathName() + "/Transactions/allLogs-q.txt");
    File sFile = new File(sharedData.getFilePathName() + "/Transactions/allLogs-s.txt");
    File tFile = new File(sharedData.getFilePathName() + "/Transactions/allLogs-t.txt");

    LogTailerListener qListener = new LogTailerListener(IncrementalLog.TYPE_QUERY, incrementalLogQueue, resume);
    LogTailerListener sListener = new LogTailerListener(IncrementalLog.TYPE_STATEMENT, incrementalLogQueue, resume);
    LogTailerListener tListener = new LogTailerListener(IncrementalLog.TYPE_TRANSACTION, incrementalLogQueue, resume);

    qLogTailer = new LogTailer(qFile, qListener, 1000, -1);
    sLogTailer = new LogTailer(sFile, sListener, 1000, -1);
    tLogTailer = new LogTailer(tFile, tListener, 1000, -1);

    qLogTailerThread = new Thread(qLogTailer);
    sLogTailerThread = new Thread(sLogTailer);
    tLogTailerThread = new Thread(tLogTailer);

    qLogTailerThread.start();
    sLogTailerThread.start();
    tLogTailerThread.start();

    try
    {
      Thread.sleep(100);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }

    incrementalLogSender = new IncrementalLogSender(34444, incrementalLogQueue, sharedData);
    incrementalLogThread = new Thread(incrementalLogSender);
    incrementalLogThread.start();

    isIncrementalLogging = true;
  }

  private void stopIncrementalLogging()
  {
    if (!isIncrementalLogging)
    {
      return;
    }

    if (sysLogTailerThread != null)
    {
      sysLogTailer.stop();
    }
    if (sLogTailerThread != null)
    {
      sLogTailer.stop();
    }
    if (tLogTailerThread != null)
    {
      tLogTailer.stop();
    }
    if (qLogTailerThread != null)
    {
      qLogTailer.stop();
    }
    if (incrementalLogThread != null)
    {
      incrementalLogSender.setTerminate(true);
    }

    try
    {
      if (sysLogTailerThread != null)
      {
        sysLogTailerThread.interrupt();
        sysLogTailerThread.join();
      }
      if (sLogTailerThread != null)
      {
        sLogTailerThread.interrupt();
        sLogTailerThread.join();
      }
      if (tLogTailerThread != null)
      {
        tLogTailerThread.interrupt();
        tLogTailerThread.join();
      }
      if (qLogTailerThread != null)
      {
        qLogTailerThread.interrupt();
        qLogTailerThread.join();
      }
      if (incrementalLogThread != null)
      {
        incrementalLogThread.interrupt();
        incrementalLogThread.join();
      }
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }

    isIncrementalLogging = false;
  }

  private void offerLog(IncrementalLog log)
  {
    if (!incrementalLogQueue.offer(log))
    {
      try
      {
        Thread.sleep(250);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      incrementalLogQueue.offer(log);
    }
  }

  private class Interrupter extends Thread {
    private Thread outerThread;
    private boolean outerThreadWaiting;

    public Interrupter(Thread t) {
      outerThread = t;
      outerThreadWaiting = true;
    }

    public void setOuterThreadWaiting(boolean b) {
      outerThreadWaiting = b;
    }

    public void run() {
      try {
        Thread.sleep(60000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      if (outerThreadWaiting) {
        outerThread.interrupt();
      }

    }
  }

}
