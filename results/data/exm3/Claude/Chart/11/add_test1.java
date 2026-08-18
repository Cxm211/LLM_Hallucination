// org/jfree/chart/util/junit/ShapeUtilitiesTests.java
public void testEqualGeneralPathsWithNull() {
    GeneralPath g1 = new GeneralPath();
    g1.moveTo(1.0f, 2.0f);
    g1.lineTo(3.0f, 4.0f);
    assertFalse(ShapeUtilities.equal(g1, null));
    assertFalse(ShapeUtilities.equal(null, g1));
    assertTrue(ShapeUtilities.equal((GeneralPath)null, (GeneralPath)null));
}