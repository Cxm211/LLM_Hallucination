// ===== FIXED com.google.javascript.rhino.JSDocInfoBuilder :: recordBlockDescription(String) [lines 188-191] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-106-fixed/src/com/google/javascript/rhino/JSDocInfoBuilder.java =====
  public boolean recordBlockDescription(String description) {
    populated = true;
    return currentInfo.documentBlock(description);
  }
