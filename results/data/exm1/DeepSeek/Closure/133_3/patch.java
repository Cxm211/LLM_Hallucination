  private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    if (result != null && (result.startsWith("get ") || result.startsWith("set "))) {
      return "";
    }
    return result;
  }