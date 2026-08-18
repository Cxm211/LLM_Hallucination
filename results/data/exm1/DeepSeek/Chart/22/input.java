// buggy code
    public Object getObject(Comparable rowKey, Comparable columnKey) {
        if (rowKey == null) {
            throw new IllegalArgumentException("Null 'rowKey' argument.");
        }
        if (columnKey == null) {
            throw new IllegalArgumentException("Null 'columnKey' argument.");
        }
        int row = this.rowKeys.indexOf(rowKey);
        if (row < 0) {
            throw new UnknownKeyException("Row key (" + rowKey 
                    + ") not recognised.");
        }
        int column = this.columnKeys.indexOf(columnKey);
        if (column < 0) {
            throw new UnknownKeyException("Column key (" + columnKey 
                    + ") not recognised.");
        }
        if (row >= 0) {
        KeyedObjects rowData = (KeyedObjects) this.rows.get(row);
            return rowData.getObject(columnKey);
        }
        else {
            return null;
        }
    }

    public void removeObject(Comparable rowKey, Comparable columnKey) {
        setObject(null, rowKey, columnKey);
        
        // 1. check whether the row is now empty.
        boolean allNull = true;
        int rowIndex = getRowIndex(rowKey);
        KeyedObjects row = (KeyedObjects) this.rows.get(rowIndex);

        for (int item = 0, itemCount = row.getItemCount(); item < itemCount; 
             item++) {
            if (row.getObject(item) != null) {
                allNull = false;
                break;
            }
        }
        
        if (allNull) {
            this.rowKeys.remove(rowIndex);
            this.rows.remove(rowIndex);
        }
        
        // 2. check whether the column is now empty.
        
        
    }

    public void removeRow(Comparable rowKey) {
        int index = getRowIndex(rowKey);
        removeRow(index);
    }

    public void removeColumn(Comparable columnKey) {
        int index = getColumnIndex(columnKey);
        if (index < 0) {
            throw new UnknownKeyException("Column key (" + columnKey 
                    + ") not recognised.");
        }
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            KeyedObjects rowData = (KeyedObjects) iterator.next();
                rowData.removeValue(columnKey);
        }
        this.columnKeys.remove(columnKey);
    }

// relevant test
// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testEquals
    public void testEquals() {
        BoxAndWhiskerRenderer r1 = new BoxAndWhiskerRenderer();
        BoxAndWhiskerRenderer r2 = new BoxAndWhiskerRenderer();
        assertEquals(r1, r2);
        
        r1.setArtifactPaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.blue));
        assertFalse(r1.equals(r2));
        r2.setArtifactPaint(new GradientPaint(1.0f, 2.0f, Color.yellow, 
                3.0f, 4.0f, Color.blue));
        assertEquals(r1, r2);
        
        r1.setFillBox(!r1.getFillBox());
        assertFalse(r1.equals(r2));
        r2.setFillBox(!r2.getFillBox());
        assertEquals(r1, r2);
        
        r1.setItemMargin(0.11);
        assertFalse(r1.equals(r2));
        r2.setItemMargin(0.11);
        assertEquals(r1, r2);
        
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testHashcode
    public void testHashcode() {
        BoxAndWhiskerRenderer r1 = new BoxAndWhiskerRenderer();
        BoxAndWhiskerRenderer r2 = new BoxAndWhiskerRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testCloning
    public void testCloning() {
        BoxAndWhiskerRenderer r1 = new BoxAndWhiskerRenderer();
        BoxAndWhiskerRenderer r2 = null;
        try {
            r2 = (BoxAndWhiskerRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testSerialization
    public void testSerialization() {

        BoxAndWhiskerRenderer r1 = new BoxAndWhiskerRenderer();
        BoxAndWhiskerRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (BoxAndWhiskerRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset();
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0),
                    new Double(0.0), new Double(4.0), new Double(0.5), 
                    new Double(4.5), new Double(-0.5), new Double(5.5), 
                    null), "S1", "C1");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new BoxAndWhiskerRenderer());
            JFreeChart chart = new JFreeChart(plot);
             chart.createBufferedImage(300, 200, 
                    null);
            success = true;
        }
        catch (NullPointerException e) {
            success = false;
        }
        assertTrue(success);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testBug1572478Vertical
    public void testBug1572478Vertical() {
        DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset() {
                
            public Number getQ1Value(int row, int column) {
                return null;
            }

            public Number getQ1Value(Comparable rowKey, Comparable columnKey) {
                return null;
            }
        };
        List values = new ArrayList();
        values.add(new Double(1.0));
        values.add(new Double(10.0));
        values.add(new Double(100.0));
        dataset.add(values, "row", "column");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("x"),
                new NumberAxis("y"), new BoxAndWhiskerRenderer());
        JFreeChart chart = new JFreeChart(plot);
        boolean success = false;

        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    new ChartRenderingInfo());
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }

        assertTrue(success);

    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testBug1572478Horizontal
    public void testBug1572478Horizontal() {
        DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset() {
                
            public Number getQ1Value(int row, int column) {
                return null;
            }

            public Number getQ1Value(Comparable rowKey, Comparable columnKey) {
                return null;
            }
        };
        List values = new ArrayList();
        values.add(new Double(1.0));
        values.add(new Double(10.0));
        values.add(new Double(100.0));
        dataset.add(values, "row", "column");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("x"),
                new NumberAxis("y"), new BoxAndWhiskerRenderer());
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(plot);
        boolean success = false;

        try {
            BufferedImage image = new BufferedImage(200 , 100, 
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, 
                    new ChartRenderingInfo());
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }

        assertTrue(success);

    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testGetLegendItem
    public void testGetLegendItem() {
        DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset();
        List values = new ArrayList();
        values.add(new Double(1.10));
        values.add(new Double(1.45));
        values.add(new Double(1.33));
        values.add(new Double(1.23));
        dataset.add(values, "R1", "C1");
        BoxAndWhiskerRenderer r = new BoxAndWhiskerRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("x"),
                new NumberAxis("y"), r);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(0, 0);
        assertNotNull(li);
        r.setSeriesVisibleInLegend(0, Boolean.FALSE);
        li = r.getLegendItem(0, 0);
        assertNull(li);
    }

// org.jfree.chart.renderer.category.junit.BoxAndWhiskerRendererTests::testGetLegendItemSeriesIndex
    public void testGetLegendItemSeriesIndex() {
        DefaultCategoryDataset dataset0 = new DefaultCategoryDataset();
        dataset0.addValue(21.0, "R1", "C1");
        dataset0.addValue(22.0, "R2", "C1");        
        DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
        dataset1.addValue(23.0, "R3", "C1");
        dataset1.addValue(24.0, "R4", "C1");        
        dataset1.addValue(25.0, "R5", "C1");        
        BoxAndWhiskerRenderer r = new BoxAndWhiskerRenderer();
        CategoryPlot plot = new CategoryPlot(dataset0, new CategoryAxis("x"),
                new NumberAxis("y"), r);
        plot.setDataset(1, dataset1);
         new JFreeChart(plot);
        LegendItem li = r.getLegendItem(1, 2);
        assertEquals("R5", li.getLabel());
        assertEquals(1, li.getDatasetIndex());
        assertEquals(2, li.getSeriesIndex());
    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testEquals
    public void testEquals() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        assertEquals(r1, r2);
        
        r1.setErrorIndicatorPaint(Color.red);
        assertFalse(r1.equals(r2));
        r2.setErrorIndicatorPaint(Color.red);
        assertTrue(r2.equals(r1));
    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testHashcode
    public void testHashcode() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testCloning
    public void testCloning() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = null;
        try {
            r2 = (StatisticalBarRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testSerialization
    public void testSerialization() {

        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            r2 = (StatisticalBarRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset 
                    = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(3.0, 4.0, "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new StatisticalBarRenderer());
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

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testDrawWithNullMeanVertical
    public void testDrawWithNullMeanVertical() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset 
                    = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(null, new Double(4.0), "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new StatisticalBarRenderer());
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

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testDrawWithNullMeanHorizontal
    public void testDrawWithNullMeanHorizontal() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset 
                    = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(null, new Double(4.0), "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new StatisticalBarRenderer());
            plot.setOrientation(PlotOrientation.HORIZONTAL);
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

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testDrawWithNullDeviationVertical
    public void testDrawWithNullDeviationVertical() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset 
                    = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(new Double(4.0), null, "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new StatisticalBarRenderer());
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

// org.jfree.chart.renderer.category.junit.StatisticalBarRendererTests::testDrawWithNullDeviationHorizontal
    public void testDrawWithNullDeviationHorizontal() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset 
                    = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(new Double(4.0), null, "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new StatisticalBarRenderer());
            plot.setOrientation(PlotOrientation.HORIZONTAL);
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

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testEquals
    public void testEquals() {
        StatisticalLineAndShapeRenderer r1 
            = new StatisticalLineAndShapeRenderer();
        StatisticalLineAndShapeRenderer r2 
            = new StatisticalLineAndShapeRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        
        r1.setErrorIndicatorPaint(Color.red);
        assertFalse(r1.equals(r2));
        r2.setErrorIndicatorPaint(Color.red);
        assertTrue(r2.equals(r1));
    }

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testHashcode
    public void testHashcode() {
        StatisticalLineAndShapeRenderer r1 
            = new StatisticalLineAndShapeRenderer();
        StatisticalLineAndShapeRenderer r2 
            = new StatisticalLineAndShapeRenderer();
        assertTrue(r1.equals(r2));
        int h1 = r1.hashCode();
        int h2 = r2.hashCode();
        assertEquals(h1, h2);
    }

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testCloning
    public void testCloning() {
        StatisticalLineAndShapeRenderer r1 
            = new StatisticalLineAndShapeRenderer();
        StatisticalLineAndShapeRenderer r2 = null;
        try {
            r2 = (StatisticalLineAndShapeRenderer) r1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));
    }

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testSerialization
    public void testSerialization() {

        StatisticalLineAndShapeRenderer r1 
            = new StatisticalLineAndShapeRenderer();
        StatisticalLineAndShapeRenderer r2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(r1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            r2 = (StatisticalLineAndShapeRenderer) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(r1, r2);

    }

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::testDrawWithNullInfo
    public void testDrawWithNullInfo() {
        boolean success = false;
        try {
            DefaultStatisticalCategoryDataset dataset 
                = new DefaultStatisticalCategoryDataset();
            dataset.add(1.0, 2.0, "S1", "C1");
            dataset.add(3.0, 4.0, "S1", "C2");
            CategoryPlot plot = new CategoryPlot(dataset, 
                    new CategoryAxis("Category"), new NumberAxis("Value"), 
                    new StatisticalLineAndShapeRenderer());
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

// org.jfree.chart.renderer.category.junit.StatisticalLineAndShapeRendererTests::test1562759
    public void test1562759() {
        StatisticalLineAndShapeRenderer r 
            = new StatisticalLineAndShapeRenderer(true, false);
        assertTrue(r.getBaseLinesVisible());
        assertFalse(r.getBaseShapesVisible());
        
        r = new StatisticalLineAndShapeRenderer(false, true);
        assertFalse(r.getBaseLinesVisible());
        assertTrue(r.getBaseShapesVisible());
    }

// org.jfree.data.junit.KeyedObjects2DTests::testEquals
    public void testEquals() {
        KeyedObjects2D k1 = new KeyedObjects2D();
        KeyedObjects2D k2 = new KeyedObjects2D();
        assertTrue(k1.equals(k2));
        assertTrue(k2.equals(k1));
        
        k1.addObject(new Integer(99), "R1", "C1");
        assertFalse(k1.equals(k2));
        k2.addObject(new Integer(99), "R1", "C1");
        assertTrue(k1.equals(k2)); 
    }

// org.jfree.data.junit.KeyedObjects2DTests::testCloning
    public void testCloning() {
        KeyedObjects2D o1 = new KeyedObjects2D();
        o1.setObject(new Integer(1), "V1", "C1");
        o1.setObject(null, "V2", "C1");
        o1.setObject(new Integer(3), "V3", "C2");
        KeyedObjects2D o2 = null;
        try {
            o2 = (KeyedObjects2D) o1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(o1 != o2);
        assertTrue(o1.getClass() == o2.getClass());
        assertTrue(o1.equals(o2));
        
        
        o1.addObject("XX", "R1", "C1");
        assertFalse(o1.equals(o2));
    }

// org.jfree.data.junit.KeyedObjects2DTests::testSerialization
    public void testSerialization() {

        KeyedObjects2D ko2D1 = new KeyedObjects2D();
        ko2D1.addObject(new Double(234.2), "Row1", "Col1");
        ko2D1.addObject(null, "Row1", "Col2");
        ko2D1.addObject(new Double(345.9), "Row2", "Col1");
        ko2D1.addObject(new Double(452.7), "Row2", "Col2");

        KeyedObjects2D ko2D2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(ko2D1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            ko2D2 = (KeyedObjects2D) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(ko2D1, ko2D2);

    }

// org.jfree.data.junit.KeyedObjects2DTests::testGetValueByIndex
    public void testGetValueByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("Obj1", "R1", "C1");
        data.addObject("Obj2", "R2", "C2");
        assertEquals("Obj1", data.getObject(0, 0));
        assertEquals("Obj2", data.getObject(1, 1));
        assertNull(data.getObject(0, 1));
        assertNull(data.getObject(1, 0));
        
        
        boolean pass = false;
        try {
            data.getObject(-1, 0);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            data.getObject(0, -1);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            data.getObject(2, 0);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            data.getObject(0, 2);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjects2DTests::testGetValueByKey
    public void testGetValueByKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("Obj1", "R1", "C1");
        data.addObject("Obj2", "R2", "C2");
        assertEquals("Obj1", data.getObject("R1", "C1"));
        assertEquals("Obj2", data.getObject("R2", "C2"));
        assertNull(data.getObject("R1", "C2"));
        assertNull(data.getObject("R2", "C1"));
        
        
        boolean pass = false;
        try {
            data.getObject("XX", "C1");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            data.getObject("R1", "XX");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            data.getObject("XX", "C1");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            data.getObject("R1", "XX");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjects2DTests::testSetObject
    public void testSetObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        assertEquals("Obj1", data.getObject("R1", "C1"));
        assertEquals("Obj2", data.getObject("R2", "C2"));
        assertNull(data.getObject("R1", "C2"));
        assertNull(data.getObject("R2", "C1"));
        
        
        data.setObject("ABC", "R2", "C2");
        assertEquals("ABC", data.getObject("R2", "C2"));
        
        
        boolean pass = false;
        try {
            data.setObject("X", null, "C1");
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            data.setObject("X", "R1", null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjects2DTests::testRemoveRowByIndex
    public void testRemoveRowByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        data.removeRow(0);
        assertEquals(1, data.getRowCount());
        assertEquals("Obj2", data.getObject(0, 1));
        
        
        boolean pass = false;
        try {
            data.removeRow(-1);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        
        
        pass = false;
        try {
            data.removeRow(data.getRowCount());
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjects2DTests::testRemoveColumnByIndex
    public void testRemoveColumnByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        data.removeColumn(0);
        assertEquals(1, data.getColumnCount());
        assertEquals("Obj2", data.getObject(1, 0));
        
        
        boolean pass = false;
        try {
            data.removeColumn(-1);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        
        
        pass = false;
        try {
            data.removeColumn(data.getColumnCount());
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjects2DTests::testRemoveRowByKey
    public void testRemoveRowByKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        data.removeRow("R2");
        assertEquals(1, data.getRowCount());
        assertEquals("Obj1", data.getObject(0, 0));
        
        
        boolean pass = false;
        try {
            data.removeRow("XXX");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
        
        
        pass = false;
        try {
            data.removeRow(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjects2DTests::testRemoveColumnByKey
    public void testRemoveColumnByKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        data.removeColumn("C2");
        assertEquals(1, data.getColumnCount());
        assertEquals("Obj1", data.getObject(0, 0));
        
        
        boolean pass = false;
        try {
            data.removeColumn("XXX");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
        
        
        pass = false;
        try {
            data.removeColumn(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjects2DTests::testRemoveValue
    public void testRemoveValue() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        data.removeObject("R2", "C2");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("Obj1", data.getObject(0, 0));
    }

// org.jfree.data.statistics.junit.DefaultBoxAndWhiskerCategoryDatasetTests::testEquals
    public void testEquals() {
        DefaultBoxAndWhiskerCategoryDataset d1 
                = new DefaultBoxAndWhiskerCategoryDataset();
        d1.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0), 
                new Double(3.0), new Double(4.0), new Double(5.0), 
                new Double(6.0), new Double(7.0), new Double(8.0), 
                new ArrayList()), "ROW1", "COLUMN1");
        DefaultBoxAndWhiskerCategoryDataset d2 
                = new DefaultBoxAndWhiskerCategoryDataset();
        d2.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0), 
                new Double(3.0), new Double(4.0), new Double(5.0), 
                new Double(6.0), new Double(7.0), new Double(8.0),
                new ArrayList()), "ROW1", "COLUMN1");
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));
    }

// org.jfree.data.statistics.junit.DefaultBoxAndWhiskerCategoryDatasetTests::testSerialization
    public void testSerialization() {

        DefaultBoxAndWhiskerCategoryDataset d1 
                = new DefaultBoxAndWhiskerCategoryDataset();
        d1.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0), 
                new Double(3.0), new Double(4.0), new Double(5.0), 
                new Double(6.0), new Double(7.0), new Double(8.0),
                new ArrayList()), "ROW1", "COLUMN1");
        DefaultBoxAndWhiskerCategoryDataset d2 = null;
        
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            d2 = (DefaultBoxAndWhiskerCategoryDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(d1, d2);

    }

// org.jfree.data.statistics.junit.DefaultBoxAndWhiskerCategoryDatasetTests::testCloning
    public void testCloning() {
        DefaultBoxAndWhiskerCategoryDataset d1 
                = new DefaultBoxAndWhiskerCategoryDataset();
        d1.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0), 
                new Double(3.0), new Double(4.0), new Double(5.0), 
                new Double(6.0), new Double(7.0), new Double(8.0),
                new ArrayList()), "ROW1", "COLUMN1");
        DefaultBoxAndWhiskerCategoryDataset d2 = null;
        try {
            d2 = (DefaultBoxAndWhiskerCategoryDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
        
        
        d1.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0), 
                new Double(3.0), new Double(4.0), new Double(5.0), 
                new Double(6.0), new Double(7.0), new Double(8.0),
                new ArrayList()), "ROW2", "COLUMN1");
        assertFalse(d1.equals(d2));
    }

// org.jfree.data.statistics.junit.DefaultBoxAndWhiskerCategoryDatasetTests::test1701822
    public void test1701822() {
        DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset();
        try {
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0), 
                    new Double(3.0), new Double(4.0), new Double(5.0), 
                    new Double(6.0), null, new Double(8.0),
                    new ArrayList()), "ROW1", "COLUMN1");
            dataset.add(new BoxAndWhiskerItem(new Double(1.0), new Double(2.0), 
                    new Double(3.0), new Double(4.0), new Double(5.0), 
                    new Double(6.0), new Double(7.0), null,
                    new ArrayList()), "ROW1", "COLUMN2");
        }
        catch (NullPointerException e) {
            assertTrue(false);
        }
        
    }

// org.jfree.data.statistics.junit.DefaultBoxAndWhiskerCategoryDatasetTests::testAdd
    public void testAdd() {
        DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(1.0, 2.0, 3.0, 4.0, 
                5.0, 6.0, 7.0, 8.0, new ArrayList());
        dataset.add(item1, "R1", "C1");
       
        assertEquals(2.0, dataset.getValue("R1", "C1").doubleValue(), EPSILON);
        assertEquals(1.0, dataset.getMeanValue("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(2.0, dataset.getMedianValue("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(3.0, dataset.getQ1Value("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(4.0, dataset.getQ3Value("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(5.0, dataset.getMinRegularValue("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(6.0, dataset.getMaxRegularValue("R1", "C1").doubleValue(),
                EPSILON);
        assertEquals(7.0, dataset.getMinOutlier("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(8.0, dataset.getMaxOutlier("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(new Range(7.0, 8.0), dataset.getRangeBounds(false));
    }

// org.jfree.data.statistics.junit.DefaultBoxAndWhiskerCategoryDatasetTests::testAddUpdatesCachedRange
    public void testAddUpdatesCachedRange() {
        DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(1.0, 2.0, 3.0, 4.0, 
                5.0, 6.0, 7.0, 8.0, new ArrayList());
        dataset.add(item1, "R1", "C1");
       
        
        BoxAndWhiskerItem item2 = new BoxAndWhiskerItem(1.5, 2.5, 3.5, 4.5, 
                5.5, 6.5, 7.5, 8.5, new ArrayList());
        dataset.add(item2, "R1", "C1");

        assertEquals(2.5, dataset.getValue("R1", "C1").doubleValue(), EPSILON);
        assertEquals(1.5, dataset.getMeanValue("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(2.5, dataset.getMedianValue("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(3.5, dataset.getQ1Value("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(4.5, dataset.getQ3Value("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(5.5, dataset.getMinRegularValue("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(6.5, dataset.getMaxRegularValue("R1", "C1").doubleValue(),
                EPSILON);
        assertEquals(7.5, dataset.getMinOutlier("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(8.5, dataset.getMaxOutlier("R1", "C1").doubleValue(), 
                EPSILON);
        assertEquals(new Range(7.5, 8.5), dataset.getRangeBounds(false));
    }

// org.jfree.data.statistics.junit.DefaultBoxAndWhiskerCategoryDatasetTests::testConstructor
    public void testConstructor() {
        DefaultBoxAndWhiskerCategoryDataset dataset 
                = new DefaultBoxAndWhiskerCategoryDataset();
        assertEquals(0, dataset.getColumnCount());
        assertEquals(0, dataset.getRowCount());
        assertTrue(Double.isNaN(dataset.getRangeLowerBound(false)));
        assertTrue(Double.isNaN(dataset.getRangeUpperBound(false)));
    }

// org.jfree.data.statistics.junit.DefaultStatisticalCategoryDatasetTests::testGetRangeBounds
    public void testGetRangeBounds() {
        DefaultStatisticalCategoryDataset d 
                = new DefaultStatisticalCategoryDataset();
        
        
        assertNull(d.getRangeBounds(true));
        
        
        d.add(4.5, 1.0, "R1", "C1");
        assertEquals(new Range(4.5, 4.5), d.getRangeBounds(false));
        assertEquals(new Range(3.5, 5.5), d.getRangeBounds(true));
        
        
        d.add(0.5, 2.0, "R1", "C2");
        assertEquals(new Range(0.5, 4.5), d.getRangeBounds(false));
        assertEquals(new Range(-1.5, 5.5), d.getRangeBounds(true));
        
        
        d.add(Double.NaN, 0.0, "R1", "C3");
        assertEquals(new Range(0.5, 4.5), d.getRangeBounds(false));
        assertEquals(new Range(-1.5, 5.5), d.getRangeBounds(true));

        
        d.add(Double.NEGATIVE_INFINITY, 0.0, "R1", "C3");
        assertEquals(new Range(Double.NEGATIVE_INFINITY, 4.5), 
                d.getRangeBounds(false));
        assertEquals(new Range(Double.NEGATIVE_INFINITY, 5.5), 
                d.getRangeBounds(true));

        
        d.add(Double.POSITIVE_INFINITY, 0.0, "R1", "C3");
        assertEquals(new Range(0.5, Double.POSITIVE_INFINITY), 
                d.getRangeBounds(false));
        assertEquals(new Range(-1.5, Double.POSITIVE_INFINITY), 
                d.getRangeBounds(true));
    }

// org.jfree.data.statistics.junit.DefaultStatisticalCategoryDatasetTests::testEquals
    public void testEquals() {
        DefaultStatisticalCategoryDataset d1 
                = new DefaultStatisticalCategoryDataset();
        DefaultStatisticalCategoryDataset d2 
                = new DefaultStatisticalCategoryDataset();
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));

    }

// org.jfree.data.statistics.junit.DefaultStatisticalCategoryDatasetTests::testCloning
    public void testCloning() {
        DefaultStatisticalCategoryDataset d1 
                = new DefaultStatisticalCategoryDataset();
        d1.add(1.1, 2.2, "R1", "C1");
        d1.add(3.3, 4.4, "R1", "C2");
        d1.add(null, new Double(5.5), "R1", "C3");
        d1.add(new Double(6.6), null, "R2", "C3");
        DefaultStatisticalCategoryDataset d2 = null;
        try {
            d2 = (DefaultStatisticalCategoryDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            fail(e.toString());
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
        
        
        d1.add(1.1, 2.2, "R3", "C1");
        assertFalse(d1.equals(d2));
    }

// org.jfree.data.statistics.junit.DefaultStatisticalCategoryDatasetTests::testSerialization1
    public void testSerialization1() {
        DefaultStatisticalCategoryDataset d1 
            = new DefaultStatisticalCategoryDataset();
        d1.add(1.1, 2.2, "R1", "C1");
        d1.add(3.3, 4.4, "R1", "C2");
        d1.add(null, new Double(5.5), "R1", "C3");
        d1.add(new Double(6.6), null, "R2", "C3");
        DefaultStatisticalCategoryDataset d2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            d2 = (DefaultStatisticalCategoryDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(d1, d2);
    }

// org.jfree.data.statistics.junit.DefaultStatisticalCategoryDatasetTests::testSerialization2
    public void testSerialization2() {
        DefaultStatisticalCategoryDataset d1 
            = new DefaultStatisticalCategoryDataset();
        d1.add(1.2, 3.4, "Row 1", "Column 1");
        DefaultStatisticalCategoryDataset d2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            d2 = (DefaultStatisticalCategoryDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            fail(e.toString());
        }
        assertEquals(d1, d2);
    }

// org.jfree.data.statistics.junit.DefaultStatisticalCategoryDatasetTests::testAdd
    public void testAdd() {
        DefaultStatisticalCategoryDataset d1 
                = new DefaultStatisticalCategoryDataset();
        d1.add(1.0, 2.0, "R1", "C1");
        assertEquals(1.0, d1.getValue("R1", "C1").doubleValue(), EPSILON);
        assertEquals(2.0, d1.getStdDevValue("R1", "C1").doubleValue(), EPSILON);
        
        
        d1.add(10.0, 20.0, "R1", "C1");
        assertEquals(10.0, d1.getValue("R1", "C1").doubleValue(), EPSILON);
        assertEquals(20.0, d1.getStdDevValue("R1", "C1").doubleValue(), EPSILON);
    }

// org.jfree.data.statistics.junit.DefaultStatisticalCategoryDatasetTests::testGetRangeLowerBound
    public void testGetRangeLowerBound() {
        DefaultStatisticalCategoryDataset d1 
                = new DefaultStatisticalCategoryDataset();
        d1.add(1.0, 2.0, "R1", "C1");
        assertEquals(1.0, d1.getRangeLowerBound(false), EPSILON);
        assertEquals(-1.0, d1.getRangeLowerBound(true), EPSILON);
    }

// org.jfree.data.statistics.junit.DefaultStatisticalCategoryDatasetTests::testGetRangeUpperBound
    public void testGetRangeUpperBound() {
        DefaultStatisticalCategoryDataset d1 
                = new DefaultStatisticalCategoryDataset();
        d1.add(1.0, 2.0, "R1", "C1");
        assertEquals(1.0, d1.getRangeUpperBound(false), EPSILON);
        assertEquals(3.0, d1.getRangeUpperBound(true), EPSILON);
    }

// org.jfree.data.statistics.junit.DefaultStatisticalCategoryDatasetTests::testGetRangeBounds2
    public void testGetRangeBounds2() {
        DefaultStatisticalCategoryDataset d1 
                = new DefaultStatisticalCategoryDataset();
        d1.add(1.0, 2.0, "R1", "C1");
        assertEquals(new Range(1.0, 1.0), d1.getRangeBounds(false));
        assertEquals(new Range(-1.0, 3.0), d1.getRangeBounds(true));
        
        d1.add(10.0, 20.0, "R1", "C1");
        assertEquals(new Range(10.0, 10.0), d1.getRangeBounds(false));
        assertEquals(new Range(-10.0, 30.0), d1.getRangeBounds(true));
    }
