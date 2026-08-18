// org/jfree/data/statistics/junit/DefaultBoxAndWhiskerCategoryDatasetTests.java
public void testReplaceMinItemWithNullOutliers() {
    DefaultBoxAndWhiskerCategoryDataset d = new DefaultBoxAndWhiskerCategoryDataset();
    d.add(new BoxAndWhiskerItem(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 5.0, 10.0, new ArrayList()), "R1", "C1");
    d.add(new BoxAndWhiskerItem(1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.0, 12.0, new ArrayList()), "R2", "C1");
    assertEquals(new Range(5.0, 12.0), d.getRangeBounds(false));
    assertEquals(new Range(5.0, 12.0), d.getRangeBounds(true));
    d.add(new BoxAndWhiskerItem(2.0, 3.0, 4.0, 5.0, 6.0, 7.0, null, null, new ArrayList()), "R1", "C1");
    assertEquals(new Range(7.0, 12.0), d.getRangeBounds(false));
    assertEquals(new Range(7.0, 12.0), d.getRangeBounds(true));
}
