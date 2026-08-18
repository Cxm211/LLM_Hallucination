static boolean isSimpleNumber(String s) {
    int len = s.length();
    if (len == 0) {
      return false;
    }
    for (int index = 0; index < len; index++) {
      char c = s.charAt(index);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    // Allow "0" specifically; disallow other numbers with a leading zero.
    return s.equals("0") || s.charAt(0) != '0';
  }