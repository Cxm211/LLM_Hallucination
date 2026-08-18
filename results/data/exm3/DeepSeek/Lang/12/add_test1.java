// org/apache/commons/lang3/RandomStringUtilsTest.java
public void testCharsArrayWithStartEndZero() {
        char[] chars = new char[]{'x', 'y', 'z'};
        Random mockRandom = new Random(67890) {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
        String result = RandomStringUtils.random(5, 0, 0, true, true, chars, mockRandom);
        assertEquals("xxxxx", result);
    }
