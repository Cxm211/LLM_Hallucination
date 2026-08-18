public static boolean isJSIdentifier(String s) {
  int length = s.length();

  if (length == 0) {
    return false;
  }

  char first = s.charAt(0);
  if (!Character.isJavaIdentifierStart(first) && first != '$' && first != '_') {
    return false;
  }

  for (int i = 1; i < length; i++) {
    char c = s.charAt(i);
    if (!Character.isJavaIdentifierPart(c) && c != '$') {
      return false;
    }
  }

  return true;
}