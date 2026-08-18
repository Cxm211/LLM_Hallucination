// ===== FIXED com.google.javascript.jscomp.CodeConsumer :: add(String) [lines 181-204] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-44-fixed/src/com/google/javascript/jscomp/CodeConsumer.java =====
  void add(String newcode) {
    maybeEndStatement();

    if (newcode.length() == 0) {
      return;
    }

    char c = newcode.charAt(0);
    if ((isWordChar(c) || c == '\\') &&
        isWordChar(getLastChar())) {
      // need space to separate. This is not pretty printing.
      // For example: "return foo;"
      append(" ");
    } else if (c == '/' && getLastChar() == '/') {
      // Do not allow a forward slash to appear after a DIV.
      // For example,
      // REGEXP DIV REGEXP
      // is valid and should print like
      // / // / /
      append(" ");
    }

    append(newcode);
  }
