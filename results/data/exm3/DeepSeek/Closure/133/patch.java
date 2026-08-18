  private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    if (result == null) {
      return null;
    }
    // Strip leading '*' and a single space after it, if present.
    if (result.startsWith("*")) {
      result = result.substring(1);
      if (result.startsWith(" ")) {
        result = result.substring(1);
      }
    }
    // Strip trailing "*/" if present.
    if (result.endsWith("*/")) {
      result = result.substring(0, result.length() - 2);
    }
    return result;
  }