// ===== FIXED com.google.javascript.jscomp.LightweightMessageFormatter :: format(JSError, boolean) [lines 66-111] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-62-fixed/src/com/google/javascript/jscomp/LightweightMessageFormatter.java =====
  private String format(JSError error, boolean warning) {
    // extract source excerpt
    SourceExcerptProvider source = getSource();
    String sourceExcerpt = source == null ? null :
        excerpt.get(
            source, error.sourceName, error.lineNumber, excerptFormatter);

    // formatting the message
    StringBuilder b = new StringBuilder();
    if (error.sourceName != null) {
      b.append(error.sourceName);
      if (error.lineNumber > 0) {
        b.append(':');
        b.append(error.lineNumber);
      }
      b.append(": ");
    }

    b.append(getLevelName(warning ? CheckLevel.WARNING : CheckLevel.ERROR));
    b.append(" - ");

    b.append(error.description);
    b.append('\n');
    if (sourceExcerpt != null) {
      b.append(sourceExcerpt);
      b.append('\n');
      int charno = error.getCharno();

      // padding equal to the excerpt and arrow at the end
      // charno == sourceExpert.length() means something is missing
      // at the end of the line
      if (excerpt.equals(LINE)
          && 0 <= charno && charno <= sourceExcerpt.length()) {
        for (int i = 0; i < charno; i++) {
          char c = sourceExcerpt.charAt(i);
          if (Character.isWhitespace(c)) {
            b.append(c);
          } else {
            b.append(' ');
          }
        }
        b.append("^\n");
      }
    }
    return b.toString();
  }
