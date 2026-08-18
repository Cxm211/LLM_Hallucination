public static boolean isJSIdentifier(String s) {
  int length = s.length();
  if (length == 0) {
    return false;
  }
  int codePoint = s.codePointAt(0);
  if (!Character.isJavaIdentifierStart(codePoint)) {
    return false;
  }
  int i = Character.charCount(codePoint);
  while (i < length) {
    codePoint = s.codePointAt(i);
    if (!Character.isJavaIdentifierPart(codePoint)) {
      return false;
    }
    i += Character.charCount(codePoint);
  }
  return true;
}