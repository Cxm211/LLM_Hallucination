// org/jfree/chart/imagemap/junit/StandardToolTipTagFragmentGeneratorTests.java
public void testGenerateURLFragmentWithSingleQuote() {
    StandardToolTipTagFragmentGenerator g
            = new StandardToolTipTagFragmentGenerator();
    assertEquals(" title=\"It&#x27;s working\" alt=\"\"",
            g.generateToolTipFragment("It's working"));
}