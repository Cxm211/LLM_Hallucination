// ===== FIXED com.google.javascript.jscomp.CodeGenerator :: isSimpleNumber(String) [lines 783-795] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-128-fixed/src/com/google/javascript/jscomp/CodeGenerator.java =====
  static boolean isSimpleNumber(String s) {
    int len = s.length();
    if (len == 0) {
      return false;
    }
    for (int index = 0; index < len; index++) {
      char c = s.charAt(index);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return len == 1 || s.charAt(0) != '0';
  }
