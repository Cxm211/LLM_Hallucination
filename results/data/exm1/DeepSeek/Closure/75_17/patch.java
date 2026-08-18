static Double getStringNumberValue(String rawJsString) {
  String s = trimJsWhiteSpace(rawJsString);
  if (s.length() == 0) {
    return 0.0;
  }
  if (s.length() > 2
      && s.charAt(0) == '0'
      && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
    try {
      long value = Long.parseLong(s.substring(2), 16);
      return (double) value;
    } catch (NumberFormatException e) {
      return Double.NaN;
    }
  }
  if (s.length() > 3
      && (s.charAt(0) == '-' || s.charAt(0) == '+')
      && s.charAt(1) == '0'
      && (s.charAt(2) == 'x' || s.charAt(2) == 'X')) {
    String hex = s.substring(3);
    boolean neg = s.charAt(0) == '-';
    try {
      long value = Long.parseLong(hex, 16);
      return neg ? -(double) value : (double) value;
    } catch (NumberFormatException e) {
      return Double.NaN;
    }
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