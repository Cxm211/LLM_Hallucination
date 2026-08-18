// org/jfree/data/general/junit/DatasetUtilitiesTests.java
public void testBug2849731_OHLCDataset() {
    OHLCDataset dataset = new OHLCDataset() {
        public int getSeriesCount() { return 1; }
        public Comparable getSeriesKey(int series) { return "S1"; }
        public int getItemCount(int series) { return 2; }
        public Number getX(int series, int item) { return getXValue(series, item); }
        public double getXValue(int series, int item) {
            return item + 1.0;
        }
        public Number getY(int series, int item) { return getYValue(series, item); }
        public double getYValue(int series, int item) {
            if (item == 0) return 2.0;
            else return 1.0;
        }
        public double getHighValue(int series, int item) {
            if (item == 0) return Double.NaN;
            else return 4.0;
        }
        public double getLowValue(int series, int item) {
            if (item == 0) return Double.NaN;
            else return 3.0;
        }
        public double getOpenValue(int series, int item) {
            return Double.NaN;
        }
        public double getVolumeValue(int series, int item) {
            return 100.0;
        }
        public Number getHigh(int series, int item) { return getHighValue(series, item); }
        public Number getLow(int series, int item) { return getLowValue(series, item); }
        public Number getOpen(int series, int item) { return getOpenValue(series, item); }
        public Number getClose(int series, int item) { return getY(series, item); }
        public Number getVolume(int series, int item) { return getVolumeValue(series, item); }
    };
    Range r = DatasetUtilities.iterateRangeBounds(dataset);
    assertEquals(1.0, r.getLowerBound(), EPSILON);
    assertEquals(4.0, r.getUpperBound(), EPSILON);
}
