// buggy code
    private void updateBounds(TimePeriod period, int index) {
        
        long start = period.getStart().getTime();
        long end = period.getEnd().getTime();
        long middle = start + ((end - start) / 2);

        if (this.minStartIndex >= 0) {
            long minStart = getDataItem(this.minStartIndex).getPeriod()
                .getStart().getTime();
            if (start < minStart) {
                this.minStartIndex = index;           
            }
        }
        else {
            this.minStartIndex = index;
        }
        
        if (this.maxStartIndex >= 0) {
            long maxStart = getDataItem(this.maxStartIndex).getPeriod()
                .getStart().getTime();
            if (start > maxStart) {
                this.maxStartIndex = index;           
            }
        }
        else {
            this.maxStartIndex = index;
        }
        
        if (this.minMiddleIndex >= 0) {
            long s = getDataItem(this.minMiddleIndex).getPeriod().getStart()
                .getTime();
            long e = getDataItem(this.minMiddleIndex).getPeriod().getEnd()
                .getTime();
            long minMiddle = s + (e - s) / 2;
            if (middle < minMiddle) {
                this.minMiddleIndex = index;           
            }
        }
        else {
            this.minMiddleIndex = index;
        }
        
        if (this.maxMiddleIndex >= 0) {
            long s = getDataItem(this.minMiddleIndex).getPeriod().getStart()
                .getTime();
            long e = getDataItem(this.minMiddleIndex).getPeriod().getEnd()
                .getTime();
            long maxMiddle = s + (e - s) / 2;
            if (middle > maxMiddle) {
                this.maxMiddleIndex = index;           
            }
        }
        else {
            this.maxMiddleIndex = index;
        }
        
        if (this.minEndIndex >= 0) {
            long minEnd = getDataItem(this.minEndIndex).getPeriod().getEnd()
                .getTime();
            if (end < minEnd) {
                this.minEndIndex = index;           
            }
        }
        else {
            this.minEndIndex = index;
        }
       
        if (this.maxEndIndex >= 0) {
            long maxEnd = getDataItem(this.maxEndIndex).getPeriod().getEnd()
                .getTime();
            if (end > maxEnd) {
                this.maxEndIndex = index;           
            }
        }
        else {
            this.maxEndIndex = index;
        }
        
    }

// relevant test
// org.jfree.data.time.junit.TimePeriodValuesCollectionTests::test1161340
    public void test1161340() {
        TimePeriodValuesCollection dataset = new TimePeriodValuesCollection();
        TimePeriodValues v1 = new TimePeriodValues("V1");
        v1.add(new Day(11, 3, 2005), 1.2);
        v1.add(new Day(12, 3, 2005), 3.4);
        dataset.addSeries(v1);
        assertEquals(1, dataset.getSeriesCount());
        dataset.removeSeries(v1);
        assertEquals(0, dataset.getSeriesCount());
        
        TimePeriodValues v2 = new TimePeriodValues("V2");
        v1.add(new Day(5, 3, 2005), 1.2);
        v1.add(new Day(6, 3, 2005), 3.4);
        dataset.addSeries(v2);
        assertEquals(1, dataset.getSeriesCount());
    }

// org.jfree.data.time.junit.TimePeriodValuesCollectionTests::testEquals
    public void testEquals() {
        
        TimePeriodValuesCollection c1 = new TimePeriodValuesCollection();
        TimePeriodValuesCollection c2 = new TimePeriodValuesCollection();
        assertTrue(c1.equals(c2));
        
        c1.setXPosition(TimePeriodAnchor.END);
        assertFalse(c1.equals(c2));
        c2.setXPosition(TimePeriodAnchor.END);
        assertTrue(c1.equals(c2));
        
        TimePeriodValues v1 = new TimePeriodValues("Test");
        TimePeriodValues v2 = new TimePeriodValues("Test");
        
        c1.addSeries(v1);
        assertFalse(c1.equals(c2));
        c2.addSeries(v2);
        assertTrue(c1.equals(c2));
    }

// org.jfree.data.time.junit.TimePeriodValuesCollectionTests::testSerialization
    public void testSerialization() {
        TimePeriodValuesCollection c1 = new TimePeriodValuesCollection();
        TimePeriodValuesCollection c2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(c1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            c2 = (TimePeriodValuesCollection) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(c1, c2);
    }

// org.jfree.data.time.junit.TimePeriodValuesCollectionTests::testGetSeries
    public void testGetSeries() {
        TimePeriodValuesCollection c1 = new TimePeriodValuesCollection();
        TimePeriodValues s1 = new TimePeriodValues("Series 1");
        c1.addSeries(s1);
        assertEquals("Series 1", c1.getSeries(0).getKey());
        
        boolean pass = false;
        try {
            c1.getSeries(-1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            c1.getSeries(1);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.time.junit.TimePeriodValuesCollectionTests::testGetDomainBoundsWithoutInterval
    public void testGetDomainBoundsWithoutInterval() {
        
        TimePeriodValuesCollection dataset = new TimePeriodValuesCollection();
        Range r = dataset.getDomainBounds(false);
        assertNull(r);
        
        
        TimePeriodValues s1 = new TimePeriodValues("S1");
        s1.add(new SimpleTimePeriod(1000L, 2000L), 1.0);
        dataset.addSeries(s1);
        r = dataset.getDomainBounds(false);
        assertEquals(1500.0, r.getLowerBound(), EPSILON);
        assertEquals(1500.0, r.getUpperBound(), EPSILON);
        
        
        s1.add(new SimpleTimePeriod(1500L, 3000L), 2.0);
        r = dataset.getDomainBounds(false);
        assertEquals(1500.0, r.getLowerBound(), EPSILON);
        assertEquals(2250.0, r.getUpperBound(), EPSILON);  
    }

// org.jfree.data.time.junit.TimePeriodValuesCollectionTests::testGetDomainBoundsWithInterval
    public void testGetDomainBoundsWithInterval() {
        
        TimePeriodValuesCollection dataset = new TimePeriodValuesCollection();
        Range r = dataset.getDomainBounds(true);
        assertNull(r);
        
        
        TimePeriodValues s1 = new TimePeriodValues("S1");
        s1.add(new SimpleTimePeriod(1000L, 2000L), 1.0);
        dataset.addSeries(s1);
        r = dataset.getDomainBounds(true);
        assertEquals(1000.0, r.getLowerBound(), EPSILON);
        assertEquals(2000.0, r.getUpperBound(), EPSILON);
        
        
        s1.add(new SimpleTimePeriod(1500L, 3000L), 2.0);
        r = dataset.getDomainBounds(true);
        assertEquals(1000.0, r.getLowerBound(), EPSILON);
        assertEquals(3000.0, r.getUpperBound(), EPSILON);
        
        
        s1.add(new SimpleTimePeriod(6000L, 7000L), 1.5);
        r = dataset.getDomainBounds(true);
        assertEquals(1000.0, r.getLowerBound(), EPSILON);
        assertEquals(7000.0, r.getUpperBound(), EPSILON);

        
        s1.add(new SimpleTimePeriod(4000L, 5000L), 1.4);
        r = dataset.getDomainBounds(true);
        assertEquals(1000.0, r.getLowerBound(), EPSILON);
        assertEquals(7000.0, r.getUpperBound(), EPSILON);    
    }

// org.jfree.data.time.junit.TimePeriodValuesTests::testClone
    public void testClone() {

        TimePeriodValues series = new TimePeriodValues("Test Series");

        RegularTimePeriod jan1st2002 = new Day(1, MonthConstants.JANUARY, 2002);
        try {
            series.add(jan1st2002, new Integer(42));
        }
        catch (SeriesException e) {
            System.err.println("Problem adding to collection.");
        }

        TimePeriodValues clone = null;
        try {
            clone = (TimePeriodValues) series.clone();
            clone.setKey("Clone Series");
            try {
                clone.update(0, new Integer(10));
            }
            catch (SeriesException e) {
                System.err.println("Problem updating series.");
            }
        }
        catch (CloneNotSupportedException e) {
            assertTrue(false);
        }

        int seriesValue = series.getValue(0).intValue();
        int cloneValue = clone.getValue(0).intValue();

        assertEquals(42, seriesValue);
        assertEquals(10, cloneValue);
        assertEquals("Test Series", series.getKey());
        assertEquals("Clone Series", clone.getKey());

    }

// org.jfree.data.time.junit.TimePeriodValuesTests::testAddValue
    public void testAddValue() {

        TimePeriodValues tpvs = new TimePeriodValues("Test");
        try {
            tpvs.add(new Year(1999), new Integer(1));
        }
        catch (SeriesException e) {
            System.err.println("Problem adding to series.");
        }

        int value = tpvs.getValue(0).intValue();
        assertEquals(1, value);

    }

// org.jfree.data.time.junit.TimePeriodValuesTests::testSerialization
    public void testSerialization() {

        TimePeriodValues s1 = new TimePeriodValues("A test");
        s1.add(new Year(2000), 13.75);
        s1.add(new Year(2001), 11.90);
        s1.add(new Year(2002), null);
        s1.add(new Year(2005), 19.32);
        s1.add(new Year(2007), 16.89);
        TimePeriodValues s2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(s1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
            s2 = (TimePeriodValues) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(s1.equals(s2));

    }

// org.jfree.data.time.junit.TimePeriodValuesTests::testEquals
    public void testEquals() {
        TimePeriodValues s1 = new TimePeriodValues("Time Series 1");
        TimePeriodValues s2 = new TimePeriodValues("Time Series 2");
        boolean b1 = s1.equals(s2);
        assertFalse("b1", b1);

        s2.setKey("Time Series 1");
        boolean b2 = s1.equals(s2);
        assertTrue("b2", b2);

        
        s1.setDomainDescription("XYZ");
        assertFalse(s1.equals(s2));
        s2.setDomainDescription("XYZ");
        assertTrue(s1.equals(s2));
        
        
        s1.setDomainDescription(null);
        assertFalse(s1.equals(s2));
        s2.setDomainDescription(null);
        assertTrue(s1.equals(s2));
        
        
        s1.setRangeDescription("XYZ");
        assertFalse(s1.equals(s2));
        s2.setRangeDescription("XYZ");
        assertTrue(s1.equals(s2));
        
        
        s1.setRangeDescription(null);
        assertFalse(s1.equals(s2));
        s2.setRangeDescription(null);
        assertTrue(s1.equals(s2));

        RegularTimePeriod p1 = new Day();
        RegularTimePeriod p2 = p1.next();
        s1.add(p1, 100.0);
        s1.add(p2, 200.0);
        boolean b3 = s1.equals(s2);
        assertFalse("b3", b3);

        s2.add(p1, 100.0);
        s2.add(p2, 200.0);
        boolean b4 = s1.equals(s2);
        assertTrue("b4", b4);

    }

// org.jfree.data.time.junit.TimePeriodValuesTests::test1161329
    public void test1161329() {
        TimePeriodValues tpv = new TimePeriodValues("Test");
        RegularTimePeriod t = new Day();
        tpv.add(t, 1.0);
        t = t.next();
        tpv.add(t, 2.0);
        tpv.delete(0, 1);
        assertEquals(0, tpv.getItemCount());
        tpv.add(t, 2.0);
        assertEquals(1, tpv.getItemCount());
    }

// org.jfree.data.time.junit.TimePeriodValuesTests::testAdd
    public void testAdd() {
        TimePeriodValues tpv = new TimePeriodValues("Test");
        MySeriesChangeListener listener = new MySeriesChangeListener();
        tpv.addChangeListener(listener);
        tpv.add(new TimePeriodValue(new SimpleTimePeriod(new Date(1L), 
                new Date(3L)), 99.0));
        assertEquals(99.0, tpv.getValue(0).doubleValue(), EPSILON);
        assertEquals(tpv, listener.getLastEvent().getSource());
        
        
        boolean pass = false;
        try {
            tpv.add((TimePeriodValue) null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org.jfree.data.time.junit.TimePeriodValuesTests::testGetMinStartIndex
    public void testGetMinStartIndex() {
        TimePeriodValues s = new TimePeriodValues("Test");
        assertEquals(-1, s.getMinStartIndex());
        s.add(new SimpleTimePeriod(100L, 200L), 1.0);
        assertEquals(0, s.getMinStartIndex());
        s.add(new SimpleTimePeriod(300L, 400L), 2.0);
        assertEquals(0, s.getMinStartIndex());
        s.add(new SimpleTimePeriod(0L, 50L), 3.0);
        assertEquals(2, s.getMinStartIndex());
    }

// org.jfree.data.time.junit.TimePeriodValuesTests::testGetMaxStartIndex
    public void testGetMaxStartIndex() {
        TimePeriodValues s = new TimePeriodValues("Test");
        assertEquals(-1, s.getMaxStartIndex());
        s.add(new SimpleTimePeriod(100L, 200L), 1.0);
        assertEquals(0, s.getMaxStartIndex());
        s.add(new SimpleTimePeriod(300L, 400L), 2.0);
        assertEquals(1, s.getMaxStartIndex());
        s.add(new SimpleTimePeriod(0L, 50L), 3.0);
        assertEquals(1, s.getMaxStartIndex());
    }

// org.jfree.data.time.junit.TimePeriodValuesTests::testGetMinMiddleIndex
    public void testGetMinMiddleIndex() {
        TimePeriodValues s = new TimePeriodValues("Test");
        assertEquals(-1, s.getMinMiddleIndex());
        s.add(new SimpleTimePeriod(100L, 200L), 1.0);
        assertEquals(0, s.getMinMiddleIndex());
        s.add(new SimpleTimePeriod(300L, 400L), 2.0);
        assertEquals(0, s.getMinMiddleIndex());
        s.add(new SimpleTimePeriod(0L, 50L), 3.0);
        assertEquals(2, s.getMinMiddleIndex());
    }

// org.jfree.data.time.junit.TimePeriodValuesTests::testGetMaxMiddleIndex
    public void testGetMaxMiddleIndex() {
        TimePeriodValues s = new TimePeriodValues("Test");
        assertEquals(-1, s.getMaxMiddleIndex());
        s.add(new SimpleTimePeriod(100L, 200L), 1.0);
        assertEquals(0, s.getMaxMiddleIndex());
        s.add(new SimpleTimePeriod(300L, 400L), 2.0);
        assertEquals(1, s.getMaxMiddleIndex());
        s.add(new SimpleTimePeriod(0L, 50L), 3.0);
        assertEquals(1, s.getMaxMiddleIndex());
        s.add(new SimpleTimePeriod(150L, 200L), 4.0);
        assertEquals(1, s.getMaxMiddleIndex());
    }
