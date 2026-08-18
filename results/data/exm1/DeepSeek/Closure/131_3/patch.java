public static boolean isJSIdentifier(String s) {
      int length = s.length();

      if (length == 0 ||
          !(Character.isUnicodeIdentifierStart(s.charAt(0)) || s.charAt(0) == '$' || s.charAt(0) == '_')) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        if (
            !(Character.isUnicodeIdentifierPart(s.charAt(i)) || s.charAt(i) == '$' || s.charAt(i) == '_')) {
          return false;
        }
      }

      return true;
    }