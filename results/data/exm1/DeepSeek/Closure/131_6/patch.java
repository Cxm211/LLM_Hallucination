    public static boolean isJSIdentifier(String s) {
      int length = s.length();

      if (length == 0) {
        return false;
      }

      char c = s.charAt(0);
      if (!Character.isUnicodeIdentifierStart(c) && c != '_' && c != '$') {
        return false;
      }

      for (int i = 1; i < length; i++) {
        c = s.charAt(i);
        if (!Character.isUnicodeIdentifierPart(c) && c != '_' && c != '$') {
          return false;
        }
      }

      return true;
    }