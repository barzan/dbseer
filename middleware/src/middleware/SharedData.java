package middleware;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** 
 * The class to place data shared by all other classes
 * 
 * @author Hongyu Wu
 */
public class SharedData {
	private int maxSize;
	private int numClient;
	private String serverIpAddr;
	private int serverPortNum;
	private int middlePortNum;
	private int adminPortNum;
	private boolean endOfProgram;
	private boolean outputToFile;
	private String filePathName;
	private boolean outputFlag;
	private int numWorkers;
	private String userInfoFilePath;
	private boolean isRemoteServer;
	public String remoteServerUser;

	private long selectTime, inputTime, outputTime, returnTime;
	
  public AtomicInteger txId;
  public AtomicLong queryId;
  
  public ArrayList<TransactionData> allTransactionData;
  
  public ConcurrentSkipListMap<Integer, byte[]> allTransactions;
//  public ConcurrentLinkedQueue<byte[]> allStatementsInfo;
  public ConcurrentSkipListMap<Long, QueryData> allQueries;

  public BufferedOutputStream tAllLogFileOutputStream;
  public BufferedOutputStream sAllLogFileOutputStream;
  public BufferedOutputStream qAllLogFileOutputStream;


	public HashMap<SocketChannel, MiddleSocketChannel> socketMap;
	public Selector selector;
	public Iterator<SelectionKey> keyIterator;

	SharedData() {
		maxSize = 1024;
		numClient = 0;
		endOfProgram = false;
		outputToFile = false;
		filePathName = null;
		outputFlag = false;
		isRemoteServer = false;

    txId = new AtomicInteger(0);
    queryId = new AtomicLong(0);
		
	}

	synchronized public SelectionKey getSelectionKey() {

		while (!keyIterator.hasNext()) {
			try {
				selector.selectNow();
			} catch (IOException e) {
				e.printStackTrace();
			}
			keyIterator = selector.selectedKeys().iterator();
		}

		SelectionKey key = keyIterator.next();

		keyIterator.remove();
		return key;

	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getNumClient() {
		return numClient;
	}

	public void addClient() {
		++numClient;
	}

	public void subClient() {
		--numClient;
	}

	public int getMiddlePortNum() {
		return middlePortNum;
	}

	public void setMiddlePortNum(int middlePortNum) {
		this.middlePortNum = middlePortNum;
	}

	public int getServerPortNum() {
		return serverPortNum;
	}

	public void setServerPortNum(int serverPortNum) {
		this.serverPortNum = serverPortNum;
	}

	public String getServerIpAddr() {
		return serverIpAddr;
	}

	public void setServerIpAddr(String serverIpAddr) {
		this.serverIpAddr = serverIpAddr;
	}

	public boolean isEndOfProgram() {
		return endOfProgram;
	}

	public void setEndOfProgram(boolean endOfProgram) {
		this.endOfProgram = endOfProgram;
	}

	public boolean isOutputToFile() {
		return outputToFile;
	}

	public void setOutputToFile(boolean outputToFile) {
		this.outputToFile = outputToFile;
	}

	public String getFilePathName() {
		return filePathName;
	}

	public void setFilePathName(String filePathName) {
		this.filePathName = filePathName;
	}

	public boolean isOutputFlag() {
		return outputFlag;
	}

	public void setOutputFlag(boolean outputFlag) {
		this.outputFlag = outputFlag;
	}

	public int getNumWorkers() {
		return numWorkers;
	}

	public void setNumWorkers(int numWorkers) {
		this.numWorkers = numWorkers;
	}

	public long getSelectTime() {
		return selectTime;
	}

	public void setSelectTime(long selectTime) {
		this.selectTime = selectTime;
	}

	public long getInputTime() {
		return inputTime;
	}

	public void setInputTime(long inputTime) {
		this.inputTime = inputTime;
	}

	public long getOutputTime() {
		return outputTime;
	}

	public void setOutputTime(long outputTime) {
		this.outputTime = outputTime;
	}

	public void addSelectTime(long t) {
		this.selectTime += t;
	}

	public void addInputTime(long t) {
		this.inputTime += t;
	}

	public void addOutputTime(long t) {
		this.outputTime += t;
	}

	public long getReturnTime() {
		return returnTime;
	}

	public void setReturnTime(long returnTime) {
		this.returnTime = returnTime;
	}

	public void addReturnTime(long t) {
		this.returnTime += t;
	}

  public int getAdminPortNum() {
    return adminPortNum;
  }

  public void setAdminPortNum(int adminPortNum) {
    this.adminPortNum = adminPortNum;
  }

  public String getUserInfoFilePath() {
    return userInfoFilePath;
  }

  public void setUserInfoFilePath(String userInfoFilePath) {
    this.userInfoFilePath = userInfoFilePath;
  }

  public boolean isRemoteServer() {
    return isRemoteServer;
  }

  public void setRemoteServer(boolean isRemoteServer) {
    this.isRemoteServer = isRemoteServer;
  }

}
