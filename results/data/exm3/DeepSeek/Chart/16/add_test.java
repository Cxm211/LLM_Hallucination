// org/jfree/data/category/junit/DefaultIntervalCategoryDatasetTests.java
public void testSetCategoryKeysNonEmptyOnEmptyDataset() {
        DefaultIntervalCategoryDataset empty 
                = new DefaultIntervalCategoryDataset(new double[0][0], 
                        new double[0][0]);
        try {
            empty.setCategoryKeys(new String[]{"A"});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (RuntimeException e) {
            fail("Unexpected exception: " + e);
        }
    }
