static Double getStringNumberValue(String rawJsString) {
  // vertical tab is not always whitespace
  String s = rawJsString;
  int start = 0;
  while (start < s.length() && isStrWhiteSpaceChar(s.charAt(start)) == TernaryValue.TRUE) {
    start++;
  }
  int end = s.length();
  while (end > start && isStrWhiteSpaceChar(s.charAt(end - 1)) == TernaryValue.TRUE) {
    end--;
  }
  s = s.substring(start, end);
  if (s.length() == 0) {
    return 0.0;
  }
  if (s.length() > 2
      && s.charAt(0) == '0'
      && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
    try {
      return Double.valueOf(Integer.parseInt(s.substring(2), 16));
    } catch (NumberFormatException e) {
      return Double.NaN;
    }
  }
  if (s.length() > 3
      && (s.charAt(0) == '-' || s.charAt(0) == '+')
      && s.charAt(1) == '0'
      && (s.charAt(2) == 'x' || s.charAt(2) == 'X')) {
    return null;
  }
  if (s.equals("infinity")
      || s.equals("-infinity")
      || s.equals("+infinity")) {
    return null;
  }
  try {
    return Double.parseDouble(s);
  } catch (NumberFormatException e) {
    return Double.NaN;
  }
}