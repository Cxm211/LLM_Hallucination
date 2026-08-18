public static boolean isJSIdentifier(String s) {
  int length = s.length();

  if (length == 0 ||
      !Character.isJavaIdentifierStart(s.charAt(0))) {
    return false;
  }

  for (int i = 1; i < length; i++) {
    char c = s.charAt(i);
    if (!Character.isJavaIdentifierPart(c) ||
        c == '\u2028' || c == '\u2029') {
      return false;
    }
  }

  return true;
}