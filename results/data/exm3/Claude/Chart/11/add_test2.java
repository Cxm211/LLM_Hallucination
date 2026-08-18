// org/jfree/chart/util/junit/ShapeUtilitiesTests.java
public void testEqualGeneralPathsEmpty() {
    GeneralPath g1 = new GeneralPath();
    GeneralPath g2 = new GeneralPath();
    assertTrue(ShapeUtilities.equal(g1, g2));
}