private String getRemainingJSDocLine() {
    StringBuilder sb = new StringBuilder();
    int c;
    while (true) {
      c = stream.getChar();
      if (c == -1 || c == '\n' || c == '\r') {
        break;
      }
      sb.append((char) c);
    }
    if (c == '\r') {
      stream.getChar();
    }
    String result = sb.length() == 0 ? null : sb.toString();
    return result;
  }