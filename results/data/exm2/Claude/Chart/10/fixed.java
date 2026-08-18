// ===== FIXED org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator :: generateToolTipFragment(String) [lines 64-67] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-10-fixed/source/org/jfree/chart/imagemap/StandardToolTipTagFragmentGenerator.java =====
    public String generateToolTipFragment(String toolTipText) {
        return " title=\"" + ImageMapUtilities.htmlEscape(toolTipText) 
            + "\" alt=\"\"";
    }
