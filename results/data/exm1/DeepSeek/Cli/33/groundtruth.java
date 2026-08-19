// ===== FIXED org.apache.commons.cli.HelpFormatter :: printWrapped(PrintWriter, int, int, String) [lines 726-732] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-33-fixed/src/main/java/org/apache/commons/cli/HelpFormatter.java =====
    public void printWrapped(PrintWriter pw, int width, int nextLineTabStop, String text)
    {
        StringBuffer sb = new StringBuffer(text.length());

        renderWrappedTextBlock(sb, width, nextLineTabStop, text);
        pw.println(sb.toString());
    }
