  static boolean isSimpleNumber(String s) {
    if (s == null) {
      return false;
    }
    s = s.trim();
    int len = s.length();
    if (len == 0) {
      return false;
    }
    int start = 0;
    char first = s.charAt(0);
    if (first == '+' || first == '-') {
      if (len == 1) {
        return false;
      }
      start = 1;
    }
    for (int index = start; index < len; index++) {
      char c = s.charAt(index);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return true;
  }