static String trimJsWhiteSpace(String raw) {
  int len = raw.length();
  int start = 0;
  while (start < len && isStrWhiteSpaceChar(raw.charAt(start)) == TernaryValue.TRUE) {
    start++;
  }
  int end = len;
  while (end > start && isStrWhiteSpaceChar(raw.charAt(end-1)) == TernaryValue.TRUE) {
    end--;
  }
  return raw.substring(start, end);
}