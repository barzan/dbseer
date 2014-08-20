package middleware;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 * @author Sheng Liang
 * 
 */
public class Encrypt {
  public static final int MAX_LENGTH = 30;

  public static byte[] encrypt(byte[] info) {
    MessageDigest md5 = null;
    try {
      md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    md5.update(info);
    byte[] resultBytes = md5.digest();
    return resultBytes;
  }

  public static Map<String, byte[]> getUsrMap(String filePath) {
    Map<String, byte[]> usrInfo = new HashMap<String, byte[]>();
    BufferedReader br = null;
    try {
      String sCurrentLine;
      br = new BufferedReader(new FileReader(filePath));
      while ((sCurrentLine = br.readLine()) != null) {
        if (!sCurrentLine.replaceAll("\\s", "").contentEquals("<user-password>")) {
          continue;
        } else if (!sCurrentLine.replaceAll("\\s", "").contentEquals("</user-password>")) {
          break;
        }
        byte[] tempchars = new byte[MAX_LENGTH];
        int i = 0, tempchar = 0;
        while ((tempchar = br.read()) != -1) {
          if (((char) tempchar) == '\n') {
            if (new String(tempchars).replaceAll("\\s", "").contentEquals("</user-password>")) {
              throw new Exception();
            }
            usrInfo.put(sCurrentLine, encrypt(tempchars));
            break;
          }
          tempchars[i++] = (byte) tempchar;
        }
        for (int j = 0; j < tempchars.length; j++)
          tempchars[j] = ' ';
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e1) {
    }
    finally {
      try {
        if (br != null)
          br.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return usrInfo;
  }

  public static void main(String[] args) {
    String password = "password2"; // password usr input
    String usrName = "usr1"; // usrname usr input
    char[] msg = new char[MAX_LENGTH];
    char[] strChar = password.toCharArray();
    for (int i = 0; i < strChar.length; i++) {
      msg[i] = strChar[i];
      strChar[i] = ' ';
    }

    byte[] resultBytes = encrypt(msg.toString().getBytes());
    String filePath = "   ";
    Map<String, byte[]> k = getUsrMap(filePath);//
    if (k.containsKey(usrName)) {
      byte[] tmp = (byte[]) k.get(usrName);
      if (Arrays.equals(tmp, resultBytes)) {
        System.out.println("get it");
      }
    }
  }

}
