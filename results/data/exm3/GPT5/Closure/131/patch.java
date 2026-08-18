public static boolean isJSIdentifier(String s) {
      int length = s.length();
      if (length == 0) {
        return false;
      }

      // Disallow reserved words that cannot be used as property names in dotted form
      // for the purposes of this conversion.
      if ("default".equals(s)) {
        return false;
      }

      // Only allow simple ASCII JS identifiers: [A-Za-z_$][A-Za-z0-9_$]*
      char ch0 = s.charAt(0);
      if (!(ch0 == '_' || ch0 == '$' || (ch0 >= 'A' && ch0 <= 'Z') || (ch0 >= 'a' && ch0 <= 'z'))) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        char ch = s.charAt(i);
        if (!((ch == '_' || ch == '$' || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')))) {
          return false;
        }
      }

      return true;
    }