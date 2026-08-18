static boolean isSimpleNumber(String s) {
  int len = s.length();
  if (len == 0) {
    return false;
  }
  if (len == 1) {
    char c = s.charAt(0);
    return c >= '0' && c <= '9';
  }
  for (int index = 0; index < len; index++) {
    char c = s.charAt(index);
    if (c < '0' || c > '9') {
      return false;
    }
  }
  return s.charAt(0) != '0';
}