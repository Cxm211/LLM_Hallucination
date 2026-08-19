// ===== FIXED com.google.javascript.jscomp.parsing.IRFactory :: handleBlockComment(Comment) [lines 251-259] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-122-fixed/src/com/google/javascript/jscomp/parsing/IRFactory.java =====
  private void handleBlockComment(Comment comment) {
    Pattern p = Pattern.compile("(/|(\n[ \t]*))\\*[ \t]*@[a-zA-Z]");
    if (p.matcher(comment.getValue()).find()) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }
