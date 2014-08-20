package middleware;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * A class encapsulate SocketChannel
 * 
 * @author Hongyu Wu
 */
public class MiddleSocketChannel {
  protected SocketChannel socketChannel;
  protected String ipAddr;
  protected int portNum;
  protected SelectionKey key;

  public boolean connectClient;

  public MiddleSocketChannel(String ip, int port) {
    ipAddr = ip;
    portNum = port;
    socketChannel = null;
    key = null;
  }

  public MiddleSocketChannel() {
    socketChannel = null;
    key = null;
  }

  public MiddleSocketChannel(SocketChannel inSock) {
    socketChannel = inSock;
    key = null;
  }

  public void sendOutput(ByteBuffer buffer, int len) {
    try {
      buffer.position(0);
      buffer.limit(len);
      socketChannel.write(buffer);
    } catch (IOException e) {
      System.out.println("Error in output");
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param buffer
   *          the buffer to hold data
   * @return the actual size of input data
   */
  public int getInput(ByteBuffer buffer) {
    int len = 0;
    try {
      len = socketChannel.read(buffer);

    } catch (IOException ioe) {
      System.out.println("Error in receive data");
      ioe.printStackTrace();
    }
    // System.out.println(len);
    return len;
  }

  /**
   * 
   * @return true if the socket channel is connected, false otherwise
   */
  public boolean isConnected() {
    return socketChannel.isConnected();
  }

  /**
   * close the socket channel
   */
  public void close() {
    try {
      socketChannel.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * register the socket channel to selector and attach the attachment
   * 
   * @param selector
   *          the selector to register
   * @param attachment
   *          the object to attach onto the selector
   */
  public void register(Selector selector, Object attachment) {
    try {
      selector.wakeup();
      key = socketChannel.register(selector, SelectionKey.OP_READ, attachment);
    } catch (ClosedChannelException e) {
      e.printStackTrace();
    }
  }
  
  public void cancelKey() {
    if (key != null) {
      key.cancel();
    }
  }

  public void setNonBlocking() {
    try {
      socketChannel.configureBlocking(false);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
