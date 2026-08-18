public static boolean isJSIdentifier(String s) {
  int length = s.length();
  if (length == 0) {
    return false;
  }
  int cp = s.codePointAt(0);
  if (!Character.isUnicodeIdentifierStart(cp) && cp != '$' && cp != '_') {
    return false;
  }
  for (int i = Character.charCount(cp); i < length; ) {
    cp = s.codePointAt(i);
    if (!Character.isUnicodeIdentifierPart(cp) && cp != '$' && cp != '_') {
      return false;
    }
    i += Character.charCount(cp);
  }
  return true;
}