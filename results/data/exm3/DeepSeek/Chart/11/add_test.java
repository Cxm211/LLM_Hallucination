// org/jfree/chart/util/junit/ShapeUtilitiesTests.java
public void testEqualGeneralPathsAdditional() {
        // Test empty vs non-empty paths
        GeneralPath empty = new GeneralPath();
        GeneralPath nonEmpty = new GeneralPath();
        nonEmpty.moveTo(1.0f, 2.0f);
        nonEmpty.lineTo(3.0f, 4.0f);
        assertFalse(ShapeUtilities.equal(empty, nonEmpty));
        assertFalse(ShapeUtilities.equal(nonEmpty, empty));
        
        // Test segment type mismatch
        GeneralPath p1 = new GeneralPath();
        p1.moveTo(0.0f, 0.0f);
        p1.lineTo(10.0f, 10.0f);
        GeneralPath p2 = new GeneralPath();
        p2.moveTo(0.0f, 0.0f);
        p2.quadTo(5.0f, 5.0f, 10.0f, 10.0f);
        assertFalse(ShapeUtilities.equal(p1, p2));
        assertFalse(ShapeUtilities.equal(p2, p1));
    }
