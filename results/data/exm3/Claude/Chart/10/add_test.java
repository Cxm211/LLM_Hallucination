// org/jfree/chart/imagemap/junit/StandardToolTipTagFragmentGeneratorTests.java
public void testGenerateURLFragmentWithAmpersand() {
    StandardToolTipTagFragmentGenerator g
            = new StandardToolTipTagFragmentGenerator();
    assertEquals(" title=\"A &amp; B\" alt=\"\"",
            g.generateToolTipFragment("A & B"));
}