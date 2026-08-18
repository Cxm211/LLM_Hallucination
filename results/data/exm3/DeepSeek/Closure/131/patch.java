    public static boolean isJSIdentifier(String s) {
      int length = s.length();

      if (length == 0) {
        return false;
      }
      char first = s.charAt(0);
      if (!( (first >= 'a' && first <= 'z') || (first >= 'A' && first <= 'Z') || first == '$' || first == '_' )) {
        return false;
      }
      for (int i = 1; i < length; i++) {
        char ch = s.charAt(i);
        if (!( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '$' || ch == '_' )) {
          return false;
        }
      }
      return true;
    }