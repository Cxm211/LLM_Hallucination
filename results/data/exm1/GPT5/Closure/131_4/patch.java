public static boolean isJSIdentifier(String s) {
      if (s == null) {
        return false;
      }
      int length = s.length();

      if (length == 0 ||
          !(Character.isLetter(s.charAt(0)) || s.charAt(0) == '$' || s.charAt(0) == '_')) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        char ch = s.charAt(i);
        if (!(Character.isLetterOrDigit(ch) || ch == '$' || ch == '_')) {
          return false;
        }
      }

      return true;
    }