private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    unreadToken();
    return result;
  }