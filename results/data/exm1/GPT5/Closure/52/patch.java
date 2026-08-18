static boolean isSimpleNumber(String s) {
    if (s == null) {
      return false;
    }
    int len = s.length();
    if (len == 0) {
      return false;
    }
    int index = 0;
    char first = s.charAt(0);
    if (first == '-') {
      if (len == 1) {
        return false;
      }
      index = 1;
    }
    for (; index < len; index++) {
      char c = s.charAt(index);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return true;
  }