static boolean isSimpleNumber(String s) {
    int len = s.length();
    if (len == 0) {
        return false;
    }
    char c = s.charAt(0);
    if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$')) {
        return false;
    }
    for (int index = 1; index < len; index++) {
        c = s.charAt(index);
        if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
              (c >= '0' && c <= '9') || c == '_' || c == '$')) {
            return false;
        }
    }
    return true;
  }