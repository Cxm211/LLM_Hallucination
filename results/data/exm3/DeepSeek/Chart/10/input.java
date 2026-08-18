// buggy function
    public String generateToolTipFragment(String toolTipText) {
        return " title=\"" + toolTipText
            + "\" alt=\"\"";
    }

// trigger testcase
// org/jfree/chart/imagemap/junit/StandardToolTipTagFragmentGeneratorTests.java::testGenerateURLFragment
public void testGenerateURLFragment() {
        StandardToolTipTagFragmentGenerator g
                = new StandardToolTipTagFragmentGenerator();
        assertEquals(" title=\"abc\" alt=\"\"",
                g.generateToolTipFragment("abc"));
        assertEquals(" title=\"Series &quot;A&quot;, 100.0\" alt=\"\"",
                g.generateToolTipFragment("Series \"A\", 100.0"));
    }
