private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    if (result == null) {
      return "";
    }
    result = result.replaceFirst("^\\s*\\*\\s?", "").trim();
    return result;
  }