// ===== FIXED org.jsoup.parser.HtmlTreeBuilder :: inSpecificScope(String[], String[], String[]) [lines 467-484] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-67-fixed/src/main/java/org/jsoup/parser/HtmlTreeBuilder.java =====
    private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes) {
        int depth = stack.size() -1;
        if (depth > MaxScopeSearchDepth) {
            depth = MaxScopeSearchDepth;
        }
        for (int pos = depth; pos >= 0; pos--) {
            Element el = stack.get(pos);
            String elName = el.nodeName();
            if (inSorted(elName, targetNames))
                return true;
            if (inSorted(elName, baseTypes))
                return false;
            if (extraTypes != null && inSorted(elName, extraTypes))
                return false;
        }
        Validate.fail("Should not be reachable");
        return false;
    }
