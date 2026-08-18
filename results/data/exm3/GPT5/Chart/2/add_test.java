// org/jfree/data/general/junit/DatasetUtilitiesTests.java::testBug2849731_3
public void testOHLCRangeFallback() {
        org.jfree.data.time.ohlc.OHLCDataItem[] items = new org.jfree.data.time.ohlc.OHLCDataItem[] {
            new org.jfree.data.time.ohlc.OHLCDataItem(new java.util.Date(0L), 1.0, Double.NaN, Double.NaN, 2.0, 0.0)
        };
        org.jfree.data.time.ohlc.DefaultOHLCDataset d = new org.jfree.data.time.ohlc.DefaultOHLCDataset("S1", items);
        Range r = DatasetUtilities.iterateRangeBounds(d, true);
        assertNotNull(r);
        assertEquals(2.0, r.getLowerBound(), EPSILON);
        assertEquals(2.0, r.getUpperBound(), EPSILON);
    }