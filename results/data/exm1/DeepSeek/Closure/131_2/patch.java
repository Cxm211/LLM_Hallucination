public static boolean isJSIdentifier(String s) {
      int length = s.length();
      if (length == 0) {
        return false;
      }
      char first = s.charAt(0);
      if (!((first >= 'a' && first <= 'z') || (first >= 'A' && first <= 'Z') || first == '_' || first == '$')) {
        return false;
      }
      for (int i = 1; i < length; i++) {
        char c = s.charAt(i);
        if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '$')) {
          return false;
        }
      }
      return true;
    }