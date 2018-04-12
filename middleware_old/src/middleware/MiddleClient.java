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
