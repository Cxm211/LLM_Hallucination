static boolean isSimpleNumber(String s) {
    int len = s.length();
    for (int index = 0; index < len; index++) {
      char c = s.charAt(index);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    // Allow a single zero, but disallow leading zeros for multi-digit numbers
    return len > 0 && (len == 1 || s.charAt(0) != '0');
  }