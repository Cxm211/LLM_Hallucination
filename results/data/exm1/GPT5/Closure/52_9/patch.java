static boolean isSimpleNumber(String s) {
    if (s == null) {
      return false;
    }
    int len = s.length();
    if (len == 0) {
      return false;
    }
    int i = 0;
    char first = s.charAt(0);
    if (first == '+' || first == '-') {
      i = 1;
      if (len == 1) {
        return false;
      }
    }
    boolean sawDigit = false;
    boolean sawDot = false;
    for (; i < len; i++) {
      char c = s.charAt(i);
      if (c >= '0' && c <= '9') {
        sawDigit = true;
        continue;
      }
      if (c == '.') {
        if (sawDot) {
          return false;
        }
        sawDot = true;
        continue;
      }
      return false;
    }
    return sawDigit;
  }