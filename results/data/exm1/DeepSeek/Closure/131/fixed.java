// ===== FIXED com.google.javascript.rhino.TokenStream :: isJSIdentifier(String) [lines 190-207] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-131-fixed/src/com/google/javascript/rhino/TokenStream.java =====
    public static boolean isJSIdentifier(String s) {
      int length = s.length();

      if (length == 0 ||
          Character.isIdentifierIgnorable(s.charAt(0)) ||
          !Character.isJavaIdentifierStart(s.charAt(0))) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        if (Character.isIdentifierIgnorable(s.charAt(i)) ||
            !Character.isJavaIdentifierPart(s.charAt(i))) {
          return false;
        }
      }

      return true;
    }
