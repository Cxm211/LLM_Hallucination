static String trimJsWhiteSpace(String s) {
  int start = 0;
  int end = s.length();
  while (start < end && isStrWhiteSpaceChar(s.charAt(start)) == TernaryValue.TRUE) {
    start++;
  }
  while (start < end && isStrWhiteSpaceChar(s.charAt(end - 1)) == TernaryValue.TRUE) {
    end--;
  }
  return s.substring(start, end);
}