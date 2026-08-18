  private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    // Strip leading '*' and any whitespace before/after it.
    result = result.replaceFirst("^\\s*\\*\\s*", "");
    return result;
  }