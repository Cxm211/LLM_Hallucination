// buggy function
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

// trigger testcase
// org/jfree/data/time/junit/TimePeriodValuesTests.java::testGetMaxMiddleIndex
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
