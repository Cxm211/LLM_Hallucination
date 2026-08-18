// org/jfree/data/statistics/junit/DefaultBoxAndWhiskerCategoryDatasetTests.java::testGetRangeBounds
public void testGetRangeBounds2() {
        DefaultBoxAndWhiskerCategoryDataset d 
                = new DefaultBoxAndWhiskerCategoryDataset();
        d.add(new BoxAndWhiskerItem(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 10.0, 
                new ArrayList()), "R1", "C1"); // max=10.0
        d.add(new BoxAndWhiskerItem(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 9.0, 
                new ArrayList()), "R2", "C1"); // max=9.0
        assertEquals(new Range(7.0, 10.0), d.getRangeBounds(false));
        assertEquals(new Range(7.0, 10.0), d.getRangeBounds(true));

        // replace the entry with the current maximum value with a lower maximum;
        // the new maximum should now come from a different item
        d.add(new BoxAndWhiskerItem(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.5, 8.5, 
                new ArrayList()), "R1", "C1");
        assertEquals(new Range(7.5, 9.0), d.getRangeBounds(false));
        assertEquals(new Range(7.5, 9.0), d.getRangeBounds(true));
    }