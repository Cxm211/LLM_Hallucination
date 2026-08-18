// org/apache/commons/compress/ArchiveUtilsTest.java
@Test
    public void sanitizeWithControlChars() {
        StringBuilder inputBuilder = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            inputBuilder.append('\n');
        }
        String input = inputBuilder.toString();
        StringBuilder expectedBuilder = new StringBuilder();
        for (int i = 0; i < 252; i++) {
            expectedBuilder.append('?');
        }
        expectedBuilder.append("...");
        String expected = expectedBuilder.toString();
        assertEquals(expected, ArchiveUtils.sanitize(input));
    }
