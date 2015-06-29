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

import java.util.Scanner;

public class NewMiddleware {

  public static void main(String[] args) {
    if (args.length < 5) {
      System.out.println("Error: too few arguments");
      System.out
          .println("Usage: ./middleware <listening_port> <MySQL IP> <MySQL Port> <Thread Number> <UserPasswordFile>");
      return;
    }
    SharedData sharedData = new SharedData();
    int middlePortNum = Integer.parseInt(args[0]);
    if (middlePortNum > 65536 || middlePortNum < 0) {
      System.out.println("Error: invalid listening port: " + middlePortNum);
      return;
    }

    String serverIpAddr = args[1];
    if (!serverIpAddr.contentEquals("127.0.0.1")
        && !serverIpAddr.contentEquals("localhost")) {
      sharedData.setRemoteServer(true);
      sharedData.remoteServerUser = serverIpAddr.substring(0,
          serverIpAddr.indexOf('@') == -1 ? 0 : serverIpAddr.indexOf('@'));
      if (sharedData.remoteServerUser.length() == 0) {
        System.out
            .println("Warning: lack of user name for remote server, will not deploy dstat monitoring");
      }
      serverIpAddr = serverIpAddr.substring(serverIpAddr.indexOf('@') + 1,
          serverIpAddr.length());
      if (!System.getProperty("user.name").contentEquals("root")) {
        System.out
            .println("Warning: running middleware by non-root user, will not sync dstat monitoring and middleware monitoring");
      }
    }

    int serverPortNum = Integer.parseInt(args[2]);
    if (serverPortNum > 65536 || serverPortNum < 0) {
      System.out.println("Error: invalid server port: " + serverPortNum);
      return;
    }

    int numThreads = Integer.parseInt(args[3]);
    if (numThreads <= 0) {
      System.out.println("Error: invalid thread number: " + numThreads);
      return;
    }

    sharedData.setUserInfoFilePath(args[4]);

    int adminPortNum = 3334;
    if (args.length > 5) {
      adminPortNum = Integer.parseInt(args[5]);
    }

    sharedData.setMaxSize(16 * 1024);
    sharedData.setServerIpAddr(serverIpAddr);
    sharedData.setServerPortNum(serverPortNum);
    sharedData.setMiddlePortNum(middlePortNum);
    sharedData.setAdminPortNum(adminPortNum);
    sharedData.setFilePathName(".");
    sharedData.setOutputToFile(false);
    sharedData.setNumWorkers(numThreads);

    // MiddleServerSocket middleServerSock = new MiddleServerSocket(sharedData);
    // middleServerSock.start();

    NewServerSocket serverSocket = new NewServerSocket(sharedData);
    serverSocket.start();

    // RequestHandler requestHandler = new RequestHandler(sharedData);
    // requestHandler.start();

    Scanner scanner = new Scanner(System.in);

    while (!sharedData.isEndOfProgram()) {

      String line = null;
      if (scanner.hasNextLine()) {
        line = scanner.nextLine();
      }
      if (line == null || line.isEmpty())
        continue;
      if (line.contentEquals("q")) {
        sharedData.setEndOfProgram(true);
      }
      else if (line.contentEquals("o")) {
        serverSocket.startMonitoring();
        sharedData.setOutputToFile(true);
      }
      else if (line.contentEquals("c")) {
        serverSocket.stopMonitoring();
        sharedData.setOutputToFile(false);
      }
      else if (line.contentEquals("f")) {
        sharedData.setOutputFlag(false);
      }
      else if (line.contentEquals("t")) {
        sharedData.setOutputFlag(true);
      }
    }
    scanner.close();

    return;
  }
}
