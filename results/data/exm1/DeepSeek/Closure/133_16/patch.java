  private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    if (result != null) {
      result = result.trim();
      if (result.startsWith("*")) {
        result = result.substring(1);
        if (result.startsWith(" ")) {
          result = result.substring(1);
        }
      }
    }
    return result;
  }