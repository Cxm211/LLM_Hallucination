  private String getRemainingJSDocLine() {
    if (stream == null) {
      return "";
    }
    String result = stream.getRemainingJSDocLine();
    return result == null ? "" : result;
  }