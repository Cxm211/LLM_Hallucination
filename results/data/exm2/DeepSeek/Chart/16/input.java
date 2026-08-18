    public DefaultIntervalCategoryDataset(Comparable[] seriesKeys,
                                          Comparable[] categoryKeys,
                                          Number[][] starts,
                                          Number[][] ends) {

        this.startData = starts;
        this.endData = ends;

        if (starts != null && ends != null) {

            String baseName = "org.jfree.data.resources.DataPackageResources";
            ResourceBundle resources = ResourceBundle.getBundle(baseName);

            int seriesCount = starts.length;
            if (seriesCount != ends.length) {
                String errMsg = "DefaultIntervalCategoryDataset: the number "
                    + "of series in the start value dataset does "
                    + "not match the number of series in the end "
                    + "value dataset.";
                throw new IllegalArgumentException(errMsg);
            }
            if (seriesCount > 0) {

                // set up the series names...
                if (seriesKeys != null) {

                    if (seriesKeys.length != seriesCount) {
                        throw new IllegalArgumentException(
                                "The number of series keys does not "
                                + "match the number of series in the data.");
                    }

                    this.seriesKeys = seriesKeys;
                }
                else {
                    String prefix = resources.getString(
                            "series.default-prefix") + " ";
                    this.seriesKeys = generateKeys(seriesCount, prefix);
                }

                // set up the category names...
                int categoryCount = starts[0].length;
                if (categoryCount != ends[0].length) {
                    String errMsg = "DefaultIntervalCategoryDataset: the "
                                + "number of categories in the start value "
                                + "dataset does not match the number of "
                                + "categories in the end value dataset.";
                    throw new IllegalArgumentException(errMsg);
                }
                if (categoryKeys != null) {
                    if (categoryKeys.length != categoryCount) {
                        throw new IllegalArgumentException(
                                "The number of category keys does not match "
                                + "the number of categories in the data.");
                    }
                    this.categoryKeys = categoryKeys;
                }
                else {
                    String prefix = resources.getString(
                            "categories.default-prefix") + " ";
                    this.categoryKeys = generateKeys(categoryCount, prefix);
                }

            }
            else {
                this.seriesKeys = null;
                this.categoryKeys = null;
            }
        }

    }

    public void setCategoryKeys(Comparable[] categoryKeys) {
        if (categoryKeys == null) {
            throw new IllegalArgumentException("Null 'categoryKeys' argument.");
        }
        if (categoryKeys.length != this.startData[0].length) {
            throw new IllegalArgumentException(
                    "The number of categories does not match the data.");
        }
        for (int i = 0; i < categoryKeys.length; i++) {
            if (categoryKeys[i] == null) {
                throw new IllegalArgumentException(
                    "DefaultIntervalCategoryDataset.setCategoryKeys(): "
                    + "null category not permitted.");
            }
        }
        this.categoryKeys = categoryKeys;
        fireDatasetChanged();
    }

// trigger testcase
public void testCloning2() {
    	DefaultIntervalCategoryDataset d1 
                = new DefaultIntervalCategoryDataset(new double[0][0], 
        		    new double[0][0]);
        DefaultIntervalCategoryDataset d2 = null;
        try {
            d2 = (DefaultIntervalCategoryDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));	
    }

public void testGetCategoryIndex() {
    	// check an empty dataset
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	assertEquals(-1, empty.getCategoryIndex("ABC"));
    }

public void testGetColumnCount() {
    	// check an empty dataset
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
        assertEquals(0, empty.getColumnCount());
    }

public void testGetColumnIndex() {
    	// check an empty dataset
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	assertEquals(-1, empty.getColumnIndex("ABC"));
    }

public void testGetRowCount() {
    	// check an empty dataset
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
        assertEquals(0, empty.getColumnCount());
    }

public void testGetRowIndex() {
    	// check an empty dataset
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	assertEquals(-1, empty.getRowIndex("ABC"));
    }

public void testGetSeriesIndex() {
    	// check an empty dataset
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	assertEquals(-1, empty.getSeriesIndex("ABC"));
    }

public void testSetCategoryKeys() {
    	// check an empty dataset
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	boolean pass = true;
    	try {
    		empty.setCategoryKeys(new String[0]);
    	}
    	catch (RuntimeException e) {
    		pass = false;
    	}
    	assertTrue(pass);
    }
