// ===== FIXED com.google.javascript.jscomp.parsing.JsDocInfoParser :: getRemainingJSDocLine() [lines 2399-2403] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-133-fixed/src/com/google/javascript/jscomp/parsing/JsDocInfoParser.java =====
  private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    unreadToken = NO_UNREAD_TOKEN;
    return result;
  }
