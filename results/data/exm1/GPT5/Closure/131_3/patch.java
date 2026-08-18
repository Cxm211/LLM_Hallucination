public static boolean isJSIdentifier(String s) {
      if (s == null) {
        return false;
      }

      int len = s.length();
      if (len == 0) {
        return false;
      }

      // If the string is quoted (e.g., 'a' or "a"), strip the quotes first.
      char first = s.charAt(0);
      char last = s.charAt(len - 1);
      if ((first == '\'' || first == '"') && first == last && len >= 2) {
        s = s.substring(1, len - 1);
      }

      int length = s.length();
      if (length == 0) {
        return false;
      }

      // Iterate over code points to properly handle surrogate pairs.
      int i = 0;
      int cp = s.codePointAt(i);
      if (!(cp == '$' || cp == '_' || Character.isLetter(cp))) {
        return false;
      }
      i += Character.charCount(cp);

      while (i < length) {
        cp = s.codePointAt(i);
        if (!(cp == '$' || cp == '_' || Character.isLetterOrDigit(cp))) {
          return false;
        }
        i += Character.charCount(cp);
      }

      return true;
    }