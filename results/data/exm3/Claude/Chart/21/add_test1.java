// org/jfree/data/statistics/junit/DefaultBoxAndWhiskerCategoryDatasetTests.java
public void testGetRangeBoundsMultipleReplacements() {
        DefaultBoxAndWhiskerCategoryDataset d1 
                = new DefaultBoxAndWhiskerCategoryDataset();
        d1.add(new BoxAndWhiskerItem(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 5.0, 15.0, 
                new ArrayList()), "R1", "C1");
        d1.add(new BoxAndWhiskerItem(2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 6.0, 12.0, 
                new ArrayList()), "R2", "C1");
        d1.add(new BoxAndWhiskerItem(3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 7.0, 10.0, 
                new ArrayList()), "R3", "C1");
        assertEquals(new Range(5.0, 15.0), d1.getRangeBounds(false));
        
        d1.add(new BoxAndWhiskerItem(1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 8.0, 11.0, 
                new ArrayList()), "R1", "C1");
        assertEquals(new Range(6.0, 12.0), d1.getRangeBounds(false));
        
        d1.add(new BoxAndWhiskerItem(2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 9.0, 9.5, 
                new ArrayList()), "R2", "C1");
        assertEquals(new Range(7.0, 11.0), d1.getRangeBounds(false));
    }