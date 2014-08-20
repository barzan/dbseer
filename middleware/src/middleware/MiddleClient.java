package middleware;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Client SocketChannel to connect to server, inherited from MiddleSocketChannel
 * 
 * @author Hongyu Wu
 * 
 */
public class MiddleClient extends MiddleSocketChannel {

  public MiddleClient(String ip, int port) {
    super(ip, port);
    connectClient = false;
  }

  public void startClient() {
    try {
      socketChannel = SocketChannel
          .open(new InetSocketAddress(ipAddr, portNum));
      socketChannel.configureBlocking(true);
    } catch (IOException ioe) {
      System.out
          .println("Error: fails to connect to server, please check server ip or port");
    }
  }

}
