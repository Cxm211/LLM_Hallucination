  static boolean isSimpleNumber(String s) {
    int len = s.length();
    if (len == 0) {
      return false;
    }
    
    // Reject strings with leading zeros (except "0" itself)
    if (len > 1 && s.charAt(0) == '0') {
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