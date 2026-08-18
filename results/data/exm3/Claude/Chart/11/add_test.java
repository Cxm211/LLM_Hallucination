// org/jfree/chart/util/junit/ShapeUtilitiesTests.java
public void testEqualGeneralPathsWithDifferentWindingRules() {
    GeneralPath g1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
    g1.moveTo(1.0f, 2.0f);
    g1.lineTo(3.0f, 4.0f);
    g1.closePath();
    GeneralPath g2 = new GeneralPath(GeneralPath.WIND_NON_ZERO);
    g2.moveTo(1.0f, 2.0f);
    g2.lineTo(3.0f, 4.0f);
    g2.closePath();
    assertFalse(ShapeUtilities.equal(g1, g2));
}