// org/jfree/chart/util/junit/ShapeUtilitiesTests.java::testEqualGeneralPaths
public void testEqualGeneralPaths_SegmentTypeMismatch() {
        GeneralPath g1 = new GeneralPath();
        g1.moveTo(1.0f, 2.0f);
        g1.lineTo(3.0f, 4.0f);
        g1.closePath();
        GeneralPath g2 = new GeneralPath();
        g2.moveTo(1.0f, 2.0f);
        g2.quadTo(2.0f, 3.0f, 3.0f, 4.0f);
        g2.closePath();
        assertFalse(ShapeUtilities.equal(g1, g2));
    }