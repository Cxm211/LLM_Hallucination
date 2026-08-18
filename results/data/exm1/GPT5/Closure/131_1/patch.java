public static boolean isJSIdentifier(String s) {
  int length = s.length();

  if (length == 0) {
    return false;
  }

  // Disallow '$' anywhere to avoid ambiguity with internal separator in
  // collapsed property names.
  if (s.indexOf('$') != -1) {
    return false;
  }

  if (!Character.isJavaIdentifierStart(s.charAt(0))) {
    return false;
  }

  for (int i = 1; i < length; i++) {
    if (!Character.isJavaIdentifierPart(s.charAt(i))) {
      return false;
    }
  }

  return true;
}