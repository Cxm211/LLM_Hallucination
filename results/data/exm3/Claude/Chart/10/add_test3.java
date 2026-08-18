// org/jfree/chart/imagemap/junit/StandardToolTipTagFragmentGeneratorTests.java
public void testGenerateURLFragmentWithMixedSpecialChars() {
    StandardToolTipTagFragmentGenerator g
            = new StandardToolTipTagFragmentGenerator();
    assertEquals(" title=\"&lt;&quot;A&amp;B&quot;&gt;\" alt=\"\"",
            g.generateToolTipFragment("<\"A&B\">"));
}