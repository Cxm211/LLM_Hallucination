private String getRemainingJSDocLine() {
  String result = stream.getRemainingJSDocLine();
  looksAhead = result.length();
  return result;
}