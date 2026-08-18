public static boolean isJSIdentifier(String s) {
      int length = s.length();

      if (length == 0) {
        return false;
      }

      // ECMAScript identifier (restricted to ASCII for our tooling needs):
      // First char: letter (A-Z or a-z), '$', or '_'
      // Subsequent chars: letter, digit (0-9), '$', or '_'
      char c0 = s.charAt(0);
      if (!((c0 >= 'A' && c0 <= 'Z') || (c0 >= 'a' && c0 <= 'z') || c0 == '$' || c0 == '_')) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        char c = s.charAt(i);
        if (!((c >= 'A' && c <= 'Z') ||
              (c >= 'a' && c <= 'z') ||
              (c >= '0' && c <= '9') ||
              c == '$' || c == '_')) {
          return false;
        }
      }

      return true;
    }