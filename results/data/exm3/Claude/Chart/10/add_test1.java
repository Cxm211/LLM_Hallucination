// org/jfree/chart/imagemap/junit/StandardToolTipTagFragmentGeneratorTests.java
public void testGenerateURLFragmentWithLessThanGreaterThan() {
    StandardToolTipTagFragmentGenerator g
            = new StandardToolTipTagFragmentGenerator();
    assertEquals(" title=\"A &lt; B &gt; C\" alt=\"\"",
            g.generateToolTipFragment("A < B > C"));
}