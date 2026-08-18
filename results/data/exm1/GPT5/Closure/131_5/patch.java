public static boolean isJSIdentifier(String s) {
      if (s == null || s.isEmpty()) {
        return false;
      }

      int i = 0;
      int cp = s.codePointAt(i);
      if (!Character.isJavaIdentifierStart(cp)) {
        return false;
      }
      i += Character.charCount(cp);

      while (i < s.length()) {
        cp = s.codePointAt(i);
        if (!Character.isJavaIdentifierPart(cp)) {
          return false;
        }
        i += Character.charCount(cp);
      }

      return true;
    }