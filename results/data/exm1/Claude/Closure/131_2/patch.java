public static boolean isJSIdentifier(String s) {
  int length = s.length();

  if (length == 0) {
    return false;
  }

  int i = 0;
  int c = s.codePointAt(i);
  if (!Character.isJavaIdentifierStart(c) && c != '$' && c != '_') {
    return false;
  }
  i += Character.charCount(c);

  while (i < length) {
    c = s.codePointAt(i);
    if (!Character.isJavaIdentifierPart(c) && c != '$') {
      return false;
    }
    i += Character.charCount(c);
  }

  return true;
}