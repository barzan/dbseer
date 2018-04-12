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
import java.nio.channels.SocketChannel;

/**
 * Server SocketChannel to connect to clients, inherited from
 * MiddleSocketChannel
 * 
 * @author Hongyu Wu
 * 
 */
public class MiddleServer extends MiddleSocketChannel {
  public TransactionData transactionData;

  public MiddleServer(SocketChannel s) {
    super();
    connectClient = true;

    socketChannel = s;
    try {
      socketChannel.configureBlocking(true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    transactionData = null;
  }

  public void startServer(TransactionData t) {

    transactionData = t;

    // System.out.println("startServer");
  }

  public int getClientPort() {
    int port = socketChannel.socket().getPort();
    return port;
  }

}
