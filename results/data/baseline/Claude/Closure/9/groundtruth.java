// ===== FIXED com.google.javascript.jscomp.ProcessCommonJSModules :: normalizeSourceName(String) [lines 116-126] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-9-fixed/src/com/google/javascript/jscomp/ProcessCommonJSModules.java =====
  private String normalizeSourceName(String filename) {
    // The DOS command shell will normalize "/" to "\", so we have to
    // wrestle it back.
    filename = filename.replace("\\", "/");

    if (filename.indexOf(filenamePrefix) == 0) {
      filename = filename.substring(filenamePrefix.length());
    }

    return filename;
  }
