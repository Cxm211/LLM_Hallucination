// org/jfree/data/category/junit/DefaultIntervalCategoryDatasetTests.java
public void testSetCategoryKeys2() {
    DefaultIntervalCategoryDataset empty 
            = new DefaultIntervalCategoryDataset(new double[0][0], 
                    new double[0][0]);
    boolean pass = false;
    try {
        empty.setCategoryKeys(new String[] {"A"});
    }
    catch (IllegalArgumentException e) {
        pass = true;
    }
    catch (Exception e) {
        pass = false;
    }
    assertTrue(pass);
}
