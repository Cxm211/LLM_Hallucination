// org/jfree/chart/renderer/category/junit/MinMaxCategoryRendererTests.java
public void testEqualsWithNulls() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        assertTrue(r1.equals(r2));

        r1.setGroupPaint(null);
        assertFalse(r1.equals(r2));
        r2.setGroupPaint(null);
        assertTrue(r1.equals(r2));

        r1.setGroupStroke(null);
        assertFalse(r1.equals(r2));
        r2.setGroupStroke(null);
        assertTrue(r1.equals(r2));
    }