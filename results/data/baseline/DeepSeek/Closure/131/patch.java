    public static boolean isJSIdentifier(String s) {
      int length = s.length();

      if (length == 0 ||
          !(Character.isLetter(s.charAt(0)) || s.charAt(0) == '_' || s.charAt(0) == '$')) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        char c = s.charAt(i);
        if (
            !(Character.isLetterOrDigit(c) || c == '_' || c == '$')) {
          return false;
        }
      }

      return true;
    }