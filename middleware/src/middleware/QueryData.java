package middleware;

import java.nio.ByteBuffer;

public class QueryData {
  
  public byte[] statementInfo;
  public ByteBuffer query;
  
  QueryData(byte[] sInfo, ByteBuffer q) {
    statementInfo = sInfo;
    query = q;
  }
  
  
}
