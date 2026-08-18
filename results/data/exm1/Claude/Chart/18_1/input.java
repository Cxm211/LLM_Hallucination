// buggy code
    public void removeValue(int index) {
        this.keys.remove(index);
        this.values.remove(index);
        if (index < this.keys.size()) {
        rebuildIndex();
        }
    }

    public void removeValue(Comparable key) {
        int index = getIndex(key);
        if (index < 0) {
			return;
        }
        removeValue(index);
    }

    public void removeColumn(Comparable columnKey) {
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
                rowData.removeValue(columnKey);
        }
        this.columnKeys.remove(columnKey);
    }

// relevant test
// org.jfree.data.junit.DefaultKeyedValuesTests::testSortByValueAscending
    public void testSortByValueAscending() {

        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("C", new Double(1.0));
        data.addValue("B", null);
        data.addValue("D", new Double(3.0));
        data.addValue("A", new Double(2.0));

        data.sortByValues(SortOrder.ASCENDING);

        
        assertEquals(data.getKey(0), "C");
        assertEquals(data.getKey(1), "A");
        assertEquals(data.getKey(2), "D");
        assertEquals(data.getKey(3), "B");

        
        assertEquals(data.getValue("A"), new Double(2.0));
        assertEquals(data.getValue("B"), null);
        assertEquals(data.getValue("C"), new Double(1.0));
        assertEquals(data.getValue("D"), new Double(3.0));

        
        assertEquals(data.getValue(0), new Double(1.0));
        assertEquals(data.getValue(1), new Double(2.0));
        assertEquals(data.getValue(2), new Double(3.0));
        assertEquals(data.getValue(3), null);

    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testSortByValueDescending
    public void testSortByValueDescending() {

        DefaultKeyedValues data = new DefaultKeyedValues();
        data.addValue("C", new Double(1.0));
        data.addValue("B", null);
        data.addValue("D", new Double(3.0));
        data.addValue("A", new Double(2.0));

        data.sortByValues(SortOrder.DESCENDING);

        
        assertEquals(data.getKey(0), "D");
        assertEquals(data.getKey(1), "A");
        assertEquals(data.getKey(2), "C");
        assertEquals(data.getKey(3), "B");

        
        assertEquals(data.getValue("A"), new Double(2.0));
        assertEquals(data.getValue("B"), null);
        assertEquals(data.getValue("C"), new Double(1.0));
        assertEquals(data.getValue("D"), new Double(3.0));

        
        assertEquals(data.getValue(0), new Double(3.0));
        assertEquals(data.getValue(1), new Double(2.0));
        assertEquals(data.getValue(2), new Double(1.0));
        assertEquals(data.getValue(3), null);

    }

// org.jfree.data.junit.DefaultKeyedValuesTests::testSerialization
    public void testSerialization() {

        DefaultKeyedValues v1 = new DefaultKeyedValues();
        v1.addValue("Key 1", new Double(23));
        v1.addValue("Key 2", null);
        v1.addValue("Key 3", new Double(42));

        DefaultKeyedValues v2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(v1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            v2 = (DefaultKeyedValues) in.readObject();
            in.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        assertEquals(v1, v2);

    }

// org.jfree.data.junit.KeyedObjectTests::testEquals
    public void testEquals() {
        
        KeyedObject ko1 = new KeyedObject("Test", "Object");
        KeyedObject ko2 = new KeyedObject("Test", "Object");
        assertTrue(ko1.equals(ko2));
        assertTrue(ko2.equals(ko1));

        ko1 = new KeyedObject("Test 1", "Object");
        ko2 = new KeyedObject("Test 2", "Object");
        assertFalse(ko1.equals(ko2));

        ko1 = new KeyedObject("Test", "Object 1");
        ko2 = new KeyedObject("Test", "Object 2");
        assertFalse(ko1.equals(ko2));

    }

// org.jfree.data.junit.KeyedObjectTests::testCloning
    public void testCloning() {
        KeyedObject ko1 = new KeyedObject("Test", "Object");
        KeyedObject ko2 = null;
        try {
            ko2 = (KeyedObject) ko1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(ko1 != ko2);
        assertTrue(ko1.getClass() == ko2.getClass());
        assertTrue(ko1.equals(ko2));
    }

// org.jfree.data.junit.KeyedObjectTests::testCloning2
    public void testCloning2() {
        
        Object obj1 = new ArrayList();
        KeyedObject ko1 = new KeyedObject("Test", obj1);
        KeyedObject ko2 = null;
        try {
            ko2 = (KeyedObject) ko1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(ko1 != ko2);
        assertTrue(ko1.getClass() == ko2.getClass());
        assertTrue(ko1.equals(ko2));
        
        
        assertTrue(ko2.getObject() == obj1); 
        
        
        obj1 = new DefaultPieDataset();
        ko1 = new KeyedObject("Test", obj1);
        ko2 = null;
        try {
            ko2 = (KeyedObject) ko1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(ko1 != ko2);
        assertTrue(ko1.getClass() == ko2.getClass());
        assertTrue(ko1.equals(ko2));
        
        
        assertTrue(ko2.getObject() != obj1); 
    }

// org.jfree.data.junit.KeyedObjectTests::testSerialization
    public void testSerialization() {

        KeyedObject ko1 = new KeyedObject("Test", "Object");
        KeyedObject ko2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(ko1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            ko2 = (KeyedObject) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(ko1, ko2);

    }

// org.jfree.data.junit.KeyedObjectsTests::testCloning
    public void testCloning() {
        KeyedObjects ko1 = new KeyedObjects();
        ko1.addObject("V1", new Integer(1));
        ko1.addObject("V2", null);
        ko1.addObject("V3", new Integer(3));
        KeyedObjects ko2 = null;
        try {
            ko2 = (KeyedObjects) ko1.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone.");
        }
        assertTrue(ko1 != ko2);
        assertTrue(ko1.getClass() == ko2.getClass());
        assertTrue(ko1.equals(ko2));
    }

// org.jfree.data.junit.KeyedObjectsTests::testCloning2
    public void testCloning2() {
        
        Object obj1 = new ArrayList();
        KeyedObjects ko1 = new KeyedObjects();
        ko1.addObject("K1", obj1);
        KeyedObjects ko2 = null;
        try {
            ko2 = (KeyedObjects) ko1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(ko1 != ko2);
        assertTrue(ko1.getClass() == ko2.getClass());
        assertTrue(ko1.equals(ko2));
        
        
        assertTrue(ko2.getObject("K1") == obj1); 
        
        
        obj1 = new DefaultPieDataset();
        ko1 = new KeyedObjects();
        ko1.addObject("K1", obj1);
        ko2 = null;
        try {
            ko2 = (KeyedObjects) ko1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(ko1 != ko2);
        assertTrue(ko1.getClass() == ko2.getClass());
        assertTrue(ko1.equals(ko2));
        
        
        assertTrue(ko2.getObject("K1") != obj1); 
    }

// org.jfree.data.junit.KeyedObjectsTests::testInsertAndRetrieve
    public void testInsertAndRetrieve() {

        KeyedObjects data = new KeyedObjects();
        data.addObject("A", new Double(1.0));
        data.addObject("B", new Double(2.0));
        data.addObject("C", new Double(3.0));
        data.addObject("D", null);

        
        assertEquals(data.getKey(0), "A");
        assertEquals(data.getKey(1), "B");
        assertEquals(data.getKey(2), "C");
        assertEquals(data.getKey(3), "D");

        
        assertEquals(data.getObject("A"), new Double(1.0));
        assertEquals(data.getObject("B"), new Double(2.0));
        assertEquals(data.getObject("C"), new Double(3.0));
        assertEquals(data.getObject("D"), null);
        
        boolean pass = false;
        try {
            data.getObject("Not a key");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
        
        
        assertEquals(data.getObject(0), new Double(1.0));
        assertEquals(data.getObject(1), new Double(2.0));
        assertEquals(data.getObject(2), new Double(3.0));
        assertEquals(data.getObject(3), null);

    }

// org.jfree.data.junit.KeyedObjectsTests::testSerialization
    public void testSerialization() {

        KeyedObjects ko1 = new KeyedObjects();
        ko1.addObject("Key 1", "Object 1");
        ko1.addObject("Key 2", null);
        ko1.addObject("Key 3", "Object 2");

        KeyedObjects ko2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(ko1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            ko2 = (KeyedObjects) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(ko1, ko2);

    }

// org.jfree.data.junit.KeyedObjectsTests::testGetObject
    public void testGetObject() {
        
        KeyedObjects ko1 = new KeyedObjects();
        ko1.addObject("Key 1", "Object 1");
        ko1.addObject("Key 2", null);
        ko1.addObject("Key 3", "Object 2");
        assertEquals("Object 1", ko1.getObject(0));
        assertNull(ko1.getObject(1));
        assertEquals("Object 2", ko1.getObject(2));
        
        
        boolean pass = false;
        try {
            ko1.getObject(-1);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        
        
        pass = false;
        try {
            ko1.getObject(3);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjectsTests::testGetKey
    public void testGetKey() {
        
        KeyedObjects ko1 = new KeyedObjects();
        ko1.addObject("Key 1", "Object 1");
        ko1.addObject("Key 2", null);
        ko1.addObject("Key 3", "Object 2");
        assertEquals("Key 1", ko1.getKey(0));
        assertEquals("Key 2", ko1.getKey(1));
        assertEquals("Key 3", ko1.getKey(2));
        
        
        boolean pass = false;
        try {
            ko1.getKey(-1);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        
        
        pass = false;
        try {
            ko1.getKey(3);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjectsTests::testGetIndex
    public void testGetIndex() {
        KeyedObjects ko1 = new KeyedObjects();
        ko1.addObject("Key 1", "Object 1");
        ko1.addObject("Key 2", null);
        ko1.addObject("Key 3", "Object 2");
        assertEquals(0, ko1.getIndex("Key 1"));
        assertEquals(1, ko1.getIndex("Key 2"));
        assertEquals(2, ko1.getIndex("Key 3"));
        
        
        boolean pass = false;
        try {
            ko1.getIndex(null);   
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjectsTests::testSetObject
    public void testSetObject() {
        KeyedObjects ko1 = new KeyedObjects();
        ko1.setObject("Key 1", "Object 1");
        ko1.setObject("Key 2", null);
        ko1.setObject("Key 3", "Object 2");
        
        assertEquals("Object 1", ko1.getObject("Key 1"));
        assertEquals(null, ko1.getObject("Key 2"));
        assertEquals("Object 2", ko1.getObject("Key 3"));
        
        
        ko1.setObject("Key 2", "AAA");
        ko1.setObject("Key 3", "BBB");
        assertEquals("AAA", ko1.getObject("Key 2"));
        assertEquals("BBB", ko1.getObject("Key 3"));
        
        
        boolean pass = false;
        try {
            ko1.setObject(null, "XX");
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjectsTests::testRemoveValue
    public void testRemoveValue() {
        KeyedObjects ko1 = new KeyedObjects();
        ko1.setObject("Key 1", "Object 1");
        ko1.setObject("Key 2", null);
        ko1.setObject("Key 3", "Object 2");
        
        ko1.removeValue(1);
        assertEquals(2, ko1.getItemCount());
        assertEquals(1, ko1.getIndex("Key 3"));
        
        ko1.removeValue("Key 1");
        assertEquals(1, ko1.getItemCount());
        assertEquals(0, ko1.getIndex("Key 3"));
        
        
        boolean pass = false;
        try {
            ko1.removeValue("UNKNOWN");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
        
        
        pass = false;
        try {
            ko1.removeValue(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.junit.KeyedObjectsTests::testRemoveValueInt
    public void testRemoveValueInt() {
        KeyedObjects ko1 = new KeyedObjects();
        ko1.setObject("Key 1", "Object 1");
        ko1.setObject("Key 2", null);
        ko1.setObject("Key 3", "Object 2");
        
        ko1.removeValue(1);
        assertEquals(2, ko1.getItemCount());
        assertEquals(1, ko1.getIndex("Key 3"));
        
        
        
        boolean pass = false;
        try {
            ko1.removeValue(-1);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        
        
        pass = false;
        try {
            ko1.removeValue(2);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.time.junit.TimeTableXYDatasetTests::testStandard
    public void testStandard() {
        TimeTableXYDataset d = new TimeTableXYDataset();
        d.add(new Year(1999), 1.0, "Series 1");
        assertEquals(d.getItemCount(), 1);
        assertEquals(d.getSeriesCount(), 1);
        d.add(new Year(2000), 2.0, "Series 2");
        assertEquals(d.getItemCount(), 2);
        assertEquals(d.getSeriesCount(), 2);
        assertEquals(d.getYValue(0, 0), 1.0, DELTA);
        assertTrue(Double.isNaN(d.getYValue(0, 1)));
        assertTrue(Double.isNaN(d.getYValue(1, 0)));
        assertEquals(d.getYValue(1, 1), 2.0, DELTA);
    }

// org.jfree.data.time.junit.TimeTableXYDatasetTests::testGetTimePeriod
    public void testGetTimePeriod()  {
        TimeTableXYDataset d = new TimeTableXYDataset();
        d.add(new Year(1999), 1.0, "Series 1");
        d.add(new Year(1998), 2.0, "Series 1");
        d.add(new Year(1996), 3.0, "Series 1");
        assertEquals(d.getTimePeriod(0), new Year(1996));
        assertEquals(d.getTimePeriod(1), new Year(1998));
        assertEquals(d.getTimePeriod(2), new Year(1999));
    }

// org.jfree.data.time.junit.TimeTableXYDatasetTests::testEquals
    public void testEquals() {
        TimeTableXYDataset d1 = new TimeTableXYDataset();
        TimeTableXYDataset d2 = new TimeTableXYDataset();
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));

        d1.add(new Year(1999), 123.4, "S1");
        assertFalse(d1.equals(d2));
        d2.add(new Year(1999), 123.4, "S1");
        assertTrue(d1.equals(d2));
        
        d1.setDomainIsPointsInTime(!d1.getDomainIsPointsInTime());
        assertFalse(d1.equals(d2));
        d2.setDomainIsPointsInTime(!d2.getDomainIsPointsInTime());
        assertTrue(d1.equals(d2));
        
        d1 = new TimeTableXYDataset(TimeZone.getTimeZone("GMT"));
        d2 = new TimeTableXYDataset(TimeZone.getTimeZone(
                "America/Los_Angeles"));
        assertFalse(d1.equals(d2));
    }

// org.jfree.data.time.junit.TimeTableXYDatasetTests::testClone
    public void testClone() {

        TimeTableXYDataset d = new TimeTableXYDataset();
        d.add(new Year(1999), 25.0, "Series");

        TimeTableXYDataset clone = null;
        try {
            clone = (TimeTableXYDataset) d.clone();
        }
        catch (CloneNotSupportedException e) {
            assertTrue(false);  
        }
        assertTrue(clone.equals(d));

        
        clone.add(new Year(2004), 1.2, "SS");
        assertFalse(clone.equals(d));
    }

// org.jfree.data.time.junit.TimeTableXYDatasetTests::testSerialization
    public void testSerialization() {

        TimeTableXYDataset d1 = new TimeTableXYDataset();
        d1.add(new Year(1999), 123.4, "S1");
        TimeTableXYDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(
                    buffer.toByteArray()));
            d2 = (TimeTableXYDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(d1.equals(d2));

    }

// org.jfree.data.time.junit.TimeTableXYDatasetTests::testClear
    public void testClear() {
        TimeTableXYDataset d = new TimeTableXYDataset();
        d.add(new Year(1999), 1.0, "Series 1");
        assertEquals(d.getItemCount(), 1);
        assertEquals(d.getSeriesCount(), 1);
        d.add(new Year(2000), 2.0, "Series 2"); 

        d.clear();
        
        assertEquals(0, d.getItemCount());
        assertEquals(0, d.getSeriesCount());
    }

// org.jfree.data.xy.junit.CategoryTableXYDatasetTests::testEquals
    public void testEquals() {
        
        CategoryTableXYDataset d1 = new CategoryTableXYDataset();
        d1.add(1.0, 1.1, "Series 1");
        d1.add(2.0, 2.2, "Series 1");
        
        CategoryTableXYDataset d2 = new CategoryTableXYDataset();
        d2.add(1.0, 1.1, "Series 1");
        d2.add(2.0, 2.2, "Series 1");
        
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));

        d1.add(3.0, 3.3, "Series 1");
        assertFalse(d1.equals(d2));

        d2.add(3.0, 3.3, "Series 1");
        assertTrue(d1.equals(d2));

    }

// org.jfree.data.xy.junit.CategoryTableXYDatasetTests::testCloning
    public void testCloning() {
        
        CategoryTableXYDataset d1 = new CategoryTableXYDataset();
        d1.add(1.0, 1.1, "Series 1");
        d1.add(2.0, 2.2, "Series 1");
        
        CategoryTableXYDataset d2 = null;
        try {
            d2 = (CategoryTableXYDataset) d1.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        assertTrue(d1 != d2);
        assertTrue(d1.getClass() == d2.getClass());
        assertTrue(d1.equals(d2));
    }

// org.jfree.data.xy.junit.CategoryTableXYDatasetTests::testSerialization
    public void testSerialization() {

        CategoryTableXYDataset d1 = new CategoryTableXYDataset();
        d1.add(1.0, 1.1, "Series 1");
        d1.add(2.0, 2.2, "Series 1");
        
        CategoryTableXYDataset d2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(d1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                new ByteArrayInputStream(buffer.toByteArray())
            );
            d2 = (CategoryTableXYDataset) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(d1, d2);

    }

// org.jfree.data.xy.junit.CategoryTableXYDatasetTests::testAddSeries
    public void testAddSeries() {
        CategoryTableXYDataset d1 = new CategoryTableXYDataset();
        d1.setAutoWidth(true);
        d1.add(3.0, 1.1, "Series 1");
        d1.add(7.0, 2.2, "Series 1");
        assertEquals(3.0, d1.getXValue(0, 0), EPSILON);
        assertEquals(7.0, d1.getXValue(0, 1), EPSILON);
        assertEquals(1.0, d1.getStartXValue(0, 0), EPSILON);
        assertEquals(5.0, d1.getStartXValue(0, 1), EPSILON);
        assertEquals(5.0, d1.getEndXValue(0, 0), EPSILON);
        assertEquals(9.0, d1.getEndXValue(0, 1), EPSILON);

        
        d1.add(7.5, 1.1, "Series 2");
        d1.add(9.0, 2.2, "Series 2");
 
        assertEquals(3.0, d1.getXValue(1, 0), EPSILON);
        assertEquals(7.0, d1.getXValue(1, 1), EPSILON);
        assertEquals(7.5, d1.getXValue(1, 2), EPSILON);
        assertEquals(9.0, d1.getXValue(1, 3), EPSILON);
        
        assertEquals(7.25, d1.getStartXValue(1, 2), EPSILON);
        assertEquals(8.75, d1.getStartXValue(1, 3), EPSILON);
        assertEquals(7.75, d1.getEndXValue(1, 2), EPSILON);
        assertEquals(9.25, d1.getEndXValue(1, 3), EPSILON);

        
        assertEquals(2.75, d1.getStartXValue(0, 0), EPSILON);
        assertEquals(6.75, d1.getStartXValue(0, 1), EPSILON);
        assertEquals(3.25, d1.getEndXValue(0, 0), EPSILON);
        assertEquals(7.25, d1.getEndXValue(0, 1), EPSILON);
    }
