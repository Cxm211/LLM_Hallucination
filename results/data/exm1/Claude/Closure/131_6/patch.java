public static boolean isJSIdentifier(String s) {
      int length = s.length();

      if (length == 0) {
        return false;
      }

      char first = s.charAt(0);
      if (!Character.isLetter(first) && first != '_' && first != '$') {
        return false;
      }

      for (int i = 1; i < length; i++) {
        char c = s.charAt(i);
        if (!Character.isLetterOrDigit(c) && c != '_' && c != '$') {
          return false;
        }
      }

      return true;
    }