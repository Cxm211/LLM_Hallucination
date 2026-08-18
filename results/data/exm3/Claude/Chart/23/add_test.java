// org/jfree/chart/renderer/category/junit/MinMaxCategoryRendererTests.java
public void testCloning() throws CloneNotSupportedException {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        r1.setDrawLines(true);
        r1.setGroupPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, Color.yellow));
        r1.setGroupStroke(new BasicStroke(2.5f));
        MinMaxCategoryRenderer r2 = (MinMaxCategoryRenderer) r1.clone();
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }