public static boolean isJSIdentifier(String s) {
  int length = s.length();
  if (length == 0) {
    return false;
  }
  char first = s.charAt(0);
  if (!( (first >= 'A' && first <= 'Z') ||
         (first >= 'a' && first <= 'z') ||
         first == '_' ||
         first == '$' )) {
    return false;
  }
  for (int i = 1; i < length; i++) {
    char ch = s.charAt(i);
    if (!( (ch >= 'A' && ch <= 'Z') ||
           (ch >= 'a' && ch <= 'z') ||
           (ch >= '0' && ch <= '9') ||
           ch == '_' ||
           ch == '$' )) {
      return false;
    }
  }
  return true;
}