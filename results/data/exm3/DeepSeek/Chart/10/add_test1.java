// org/jfree/chart/imagemap/junit/StandardToolTipTagFragmentGeneratorTests.java
public void testGenerateToolTipFragmentWithAmpersandAndQuote() {
    StandardToolTipTagFragmentGenerator g = new StandardToolTipTagFragmentGenerator();
    assertEquals(" title=\"a &amp; &quot;b&quot;\" alt=\"\"", g.generateToolTipFragment("a & \"b\""));
}
