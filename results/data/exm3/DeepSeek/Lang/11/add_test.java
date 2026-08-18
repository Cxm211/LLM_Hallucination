// org/apache/commons/lang3/RandomStringUtilsTest.java
public void testLANG807_CharsArrayValidation() {
        char[] chars = new char[]{'a','b','c','d','e'};
        int[][] invalidRanges = {
            {3,3}, // start == end
            {4,3}, // start > end
            {-1,3}, // start < 0
            {0,6} // end > chars.length
        };
        for (int[] range : invalidRanges) {
            int start = range[0];
            int end = range[1];
            try {
                RandomStringUtils.random(3, start, end, false, false, chars);
                fail("Expected IllegalArgumentException for start=" + start + ", end=" + end);
            } catch (IllegalArgumentException ex) {
                final String msg = ex.getMessage();
                assertTrue("Message (" + msg + ") should indicate invalid range", msg.contains("start") || msg.contains("end"));
            }
        }
    }
