// buggy code
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

// relevant test
// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testEquals
    public void testEquals() {
        IntervalBarRenderer r1 = new IntervalBarRenderer();
        IntervalBarRenderer r2 = new IntervalBarRenderer();
        assertEquals(r1, r2);
    }

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testHashcode
    public void testHashcode() {
        IntervalBarRenderer r1 = new IntervalBarRenderer();
        IntervalBarRenderer r2 = new IntervalBarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testCloning
    public void testCloning() {
        IntervalBarRenderer r1 = new IntervalBarRenderer();
        IntervalBarRenderer r2 = null;
        try {
            r2 = (IntervalBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testSerialization
    public void testSerialization() {

        IntervalBarRenderer r1 = new IntervalBarRenderer();
        IntervalBarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            r2 = (IntervalBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.IntervalBarRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            double[][] starts = new double[][] {{0.1, 0.2, 0.3}, 
                    {0.3, 0.4, 0.5}};
            double[][] ends = new double[][] {{0.5, 0.6, 0.7}, {0.7, 0.8, 0.9}};
            DefaultIntervalCategoryDataset dataset 
                = new DefaultIntervalCategoryDataset(starts, ends);        
            IntervalBarRenderer renderer = new IntervalBarRenderer();
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    renderer);
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    null);
            success = true;
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetValue
    public void testGetValue() {        
        double[] starts_S1 = new double[] {0.1, 0.2, 0.3};
        double[] starts_S2 = new double[] {0.3, 0.4, 0.5};
        double[] ends_S1 = new double[] {0.5, 0.6, 0.7};
        double[] ends_S2 = new double[] {0.7, 0.8, 0.9};
        double[][] starts = new double[][] {starts_S1, starts_S2};
        double[][] ends = new double[][] {ends_S1, ends_S2};
        DefaultIntervalCategoryDataset d 
                = new DefaultIntervalCategoryDataset(starts, ends);        

        assertEquals(new Double(0.1), d.getStartValue("Series 1", 
                "Category 1"));
        assertEquals(new Double(0.2), d.getStartValue("Series 1", 
                "Category 2"));
        assertEquals(new Double(0.3), d.getStartValue("Series 1", 
                "Category 3"));
        assertEquals(new Double(0.3), d.getStartValue("Series 2", 
                "Category 1"));
        assertEquals(new Double(0.4), d.getStartValue("Series 2", 
                "Category 2"));
        assertEquals(new Double(0.5), d.getStartValue("Series 2", 
                "Category 3"));
        
        assertEquals(new Double(0.5), d.getEndValue("Series 1", 
                "Category 1"));
        assertEquals(new Double(0.6), d.getEndValue("Series 1", 
                "Category 2"));
        assertEquals(new Double(0.7), d.getEndValue("Series 1", 
                "Category 3"));
        assertEquals(new Double(0.7), d.getEndValue("Series 2", 
                "Category 1"));
        assertEquals(new Double(0.8), d.getEndValue("Series 2", 
                "Category 2"));
        assertEquals(new Double(0.9), d.getEndValue("Series 2", 
                "Category 3"));

        boolean pass = false;
        try {
            d.getValue("XX", "Category 1");
        }
        catch (UnknownKeyException e) {
            pass = true;   
        }
        assertTrue(pass);
        
        pass = false;
        try {
            d.getValue("Series 1", "XX");
        }
        catch (UnknownKeyException e) {
            pass = true;   
        }
        assertTrue(pass);
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetRowAndColumnCount
    public void testGetRowAndColumnCount() {
        double[] starts_S1 = new double[] {0.1, 0.2, 0.3};
        double[] starts_S2 = new double[] {0.3, 0.4, 0.5};
        double[] ends_S1 = new double[] {0.5, 0.6, 0.7};
        double[] ends_S2 = new double[] {0.7, 0.8, 0.9};
        double[][] starts = new double[][] {starts_S1, starts_S2};
        double[][] ends = new double[][] {ends_S1, ends_S2};
        DefaultIntervalCategoryDataset d 
                = new DefaultIntervalCategoryDataset(starts, ends);        

        assertEquals(2, d.getRowCount());
        assertEquals(3, d.getColumnCount());
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testEquals
    public void testEquals() {
        double[] starts_S1A = new double[] {0.1, 0.2, 0.3};
        double[] starts_S2A = new double[] {0.3, 0.4, 0.5};
        double[] ends_S1A = new double[] {0.5, 0.6, 0.7};
        double[] ends_S2A = new double[] {0.7, 0.8, 0.9};
        double[][] startsA = new double[][] {starts_S1A, starts_S2A};
        double[][] endsA = new double[][] {ends_S1A, ends_S2A};
        DefaultIntervalCategoryDataset dA 
                = new DefaultIntervalCategoryDataset(startsA, endsA);        

        double[] starts_S1B = new double[] {0.1, 0.2, 0.3};
        double[] starts_S2B = new double[] {0.3, 0.4, 0.5};
        double[] ends_S1B = new double[] {0.5, 0.6, 0.7};
        double[] ends_S2B = new double[] {0.7, 0.8, 0.9};
        double[][] startsB = new double[][] {starts_S1B, starts_S2B};
        double[][] endsB = new double[][] {ends_S1B, ends_S2B};
        DefaultIntervalCategoryDataset dB 
                = new DefaultIntervalCategoryDataset(startsB, endsB);        
            
        assertTrue(dA.equals(dB));
        assertTrue(dB.equals(dA));
        
        
    	DefaultIntervalCategoryDataset empty1 
                = new DefaultIntervalCategoryDataset(new double[0][0], 
        		        new double[0][0]);
    	DefaultIntervalCategoryDataset empty2 
                = new DefaultIntervalCategoryDataset(new double[0][0], 
		                new double[0][0]);
    	assertTrue(empty1.equals(empty2));
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testSerialization
    public void testSerialization() {

        double[] starts_S1 = new double[] {0.1, 0.2, 0.3};
        double[] starts_S2 = new double[] {0.3, 0.4, 0.5};
        double[] ends_S1 = new double[] {0.5, 0.6, 0.7};
        double[] ends_S2 = new double[] {0.7, 0.8, 0.9};
        double[][] starts = new double[][] {starts_S1, starts_S2};
        double[][] ends = new double[][] {ends_S1, ends_S2};
        DefaultIntervalCategoryDataset d1
                = new DefaultIntervalCategoryDataset(starts, ends);        
        DefaultIntervalCategoryDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            d2 = (DefaultIntervalCategoryDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(d1, d2);

    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testCloning
    public void testCloning() {
        double[] starts_S1 = new double[] {0.1, 0.2, 0.3};
        double[] starts_S2 = new double[] {0.3, 0.4, 0.5};
        double[] ends_S1 = new double[] {0.5, 0.6, 0.7};
        double[] ends_S2 = new double[] {0.7, 0.8, 0.9};
        double[][] starts = new double[][] {starts_S1, starts_S2};
        double[][] ends = new double[][] {ends_S1, ends_S2};
        DefaultIntervalCategoryDataset d1 = new DefaultIntervalCategoryDataset(
                starts, ends);
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
        
        
        d1.setStartValue(0, "Category 1", new Double(0.99));
        assertFalse(d1.equals(d2));
        d2.setStartValue(0, "Category 1", new Double(0.99));
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testCloning2
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

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testSetStartValue
    public void testSetStartValue() {
        double[] starts_S1 = new double[] {0.1, 0.2, 0.3};
        double[] starts_S2 = new double[] {0.3, 0.4, 0.5};
        double[] ends_S1 = new double[] {0.5, 0.6, 0.7};
        double[] ends_S2 = new double[] {0.7, 0.8, 0.9};
        double[][] starts = new double[][] {starts_S1, starts_S2};
        double[][] ends = new double[][] {ends_S1, ends_S2};
        DefaultIntervalCategoryDataset d1 = new DefaultIntervalCategoryDataset(
                starts, ends);
        d1.setStartValue(0, "Category 2", new Double(99.9));
        assertEquals(new Double(99.9), d1.getStartValue("Series 1", 
                "Category 2"));
        
        boolean pass = false;
        try {
            d1.setStartValue(-1, "Category 2", new Double(99.9));
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            d1.setStartValue(2, "Category 2", new Double(99.9));
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testSetEndValue
    public void testSetEndValue() {
        double[] starts_S1 = new double[] {0.1, 0.2, 0.3};
        double[] starts_S2 = new double[] {0.3, 0.4, 0.5};
        double[] ends_S1 = new double[] {0.5, 0.6, 0.7};
        double[] ends_S2 = new double[] {0.7, 0.8, 0.9};
        double[][] starts = new double[][] {starts_S1, starts_S2};
        double[][] ends = new double[][] {ends_S1, ends_S2};
        DefaultIntervalCategoryDataset d1 = new DefaultIntervalCategoryDataset(
                starts, ends);
        d1.setEndValue(0, "Category 2", new Double(99.9));
        assertEquals(new Double(99.9), d1.getEndValue("Series 1", 
                "Category 2"));
        
        boolean pass = false;
        try {
            d1.setEndValue(-1, "Category 2", new Double(99.9));
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            d1.setEndValue(2, "Category 2", new Double(99.9));
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetSeriesCount
    public void testGetSeriesCount() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	assertEquals(0, empty.getSeriesCount());
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetCategoryCount
    public void testGetCategoryCount() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	assertEquals(0, empty.getCategoryCount());
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetSeriesIndex
    public void testGetSeriesIndex() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	assertEquals(-1, empty.getSeriesIndex("ABC"));
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetRowIndex
    public void testGetRowIndex() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	assertEquals(-1, empty.getRowIndex("ABC"));
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testSetSeriesKeys
    public void testSetSeriesKeys() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	boolean pass = true;
    	try {
    		empty.setSeriesKeys(new String[0]);
    	}
    	catch (RuntimeException e) {
    		pass = false;
    	}
    	assertTrue(pass);
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetCategoryIndex
    public void testGetCategoryIndex() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	assertEquals(-1, empty.getCategoryIndex("ABC"));
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetColumnIndex
    public void testGetColumnIndex() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
    	assertEquals(-1, empty.getColumnIndex("ABC"));
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testSetCategoryKeys
    public void testSetCategoryKeys() {
    	
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

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetColumnKeys
    public void testGetColumnKeys() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
        List keys = empty.getColumnKeys();
        assertEquals(0, keys.size());
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetRowKeys
    public void testGetRowKeys() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
        List keys = empty.getRowKeys();
        assertEquals(0, keys.size());
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetColumnCount
    public void testGetColumnCount() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
        assertEquals(0, empty.getColumnCount());
    }

// org.jfree.data.category.junit.DefaultIntervalCategoryDatasetTests::testGetRowCount
    public void testGetRowCount() {
    	
    	DefaultIntervalCategoryDataset empty 
    	        = new DefaultIntervalCategoryDataset(new double[0][0], 
    	        		new double[0][0]);
        assertEquals(0, empty.getColumnCount());
    }
