  private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    if (result != null) {
      result = result.replaceFirst("^\\s*\\*", "").trim();
    }
    return result;
  }