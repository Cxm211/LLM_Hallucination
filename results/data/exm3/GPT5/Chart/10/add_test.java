// org/jfree/chart/imagemap/junit/StandardToolTipTagFragmentGeneratorTests.java::testGenerateURLFragment
public void testGenerateURLFragment_EscapesEntities() {
        StandardToolTipTagFragmentGenerator g = new StandardToolTipTagFragmentGenerator();
        assertEquals(" title=\"A &amp; B &lt;C&gt;\" alt=\"\"",
                g.generateToolTipFragment("A & B <C>"));
    }