static boolean isSimpleNumber(String s) {
    int len = s.length();
    if (len == 0) {
      return false;
    }
    // Disallow leading zeros for multi-digit numbers (only "0" is allowed)
    if (s.charAt(0) == '0' && len > 1) {
      return false;
    }
    for (int index = 0; index < len; index++) {
      char c = s.charAt(index);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return true;
  }