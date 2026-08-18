// org/jfree/chart/imagemap/junit/StandardToolTipTagFragmentGeneratorTests.java
public void testGenerateToolTipFragmentWithAmpersand() {
    StandardToolTipTagFragmentGenerator g = new StandardToolTipTagFragmentGenerator();
    assertEquals(" title=\"a &amp; b\" alt=\"\"", g.generateToolTipFragment("a & b"));
}
