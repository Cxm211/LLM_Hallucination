// org/jfree/chart/plot/junit/PiePlot3DTests.java
public void testGetMaximumExplodePercentWithNullExplodePercentages() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 1);
        PiePlot3D plot = new PiePlot3D(dataset);
        try {
            java.lang.reflect.Field field = PiePlot.class.getDeclaredField("explodePercentages");
            field.setAccessible(true);
            field.set(plot, null);
        } catch (Exception e) {
            fail("Unable to set explodePercentages to null: " + e.getMessage());
        }
        double max = plot.getMaximumExplodePercent();
        assertEquals(0.0, max, 0.0001);
    }
