static boolean isSimpleNumber(String s) {
  int len = s.length();
  if (len == 0) return false;
  if (len > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
    for (int i = 2; i < len; i++) {
      char c = s.charAt(i);
      if (!( (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') )) {
        return false;
      }
    }
    return true;
  }
  for (int index = 0; index < len; index++) {
    char c = s.charAt(index);
    if (c < '0' || c > '9') {
      return false;
    }
  }
  return true;
}