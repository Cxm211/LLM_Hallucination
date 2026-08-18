// ===== FIXED com.google.javascript.jscomp.CodeGenerator :: isSimpleNumber(String) [lines 737-746] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-52-fixed/src/com/google/javascript/jscomp/CodeGenerator.java =====
  static boolean isSimpleNumber(String s) {
    int len = s.length();
    for (int index = 0; index < len; index++) {
      char c = s.charAt(index);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return len > 0 && s.charAt(0) != '0';
  }
