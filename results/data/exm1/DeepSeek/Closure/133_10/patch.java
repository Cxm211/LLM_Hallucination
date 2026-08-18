  private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    return result == null ? null : result.trim();
  }