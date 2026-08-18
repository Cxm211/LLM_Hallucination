static boolean isSimpleNumber(String s) {
    int len = s.length();
    if (len == 0) {
      return false;
    }
    int startIndex = 0;
    if (s.charAt(0) == '-') {
      if (len == 1) {
        return false;
      }
      startIndex = 1;
    }
    for (int index = startIndex; index < len; index++) {
      char c = s.charAt(index);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return true;
  }