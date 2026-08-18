// org/apache/commons/lang3/RandomStringUtilsTest.java
public void testStartGreaterThanEnd() {
        Random mockRandom = new Random(12345) {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
        String result = RandomStringUtils.random(5, 5, 1, false, false, null, mockRandom);
        char expectedChar = (char) 1;
        String expected = new String(new char[]{expectedChar, expectedChar, expectedChar, expectedChar, expectedChar});
        assertEquals(expected, result);
    }
