// org/apache/commons/cli/bug/BugCLI252Test.java
@Test
    public void testExactOptionNameMatchWithPrefixMatches() throws ParseException {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("prefix").build());
        options.addOption(Option.builder().longOpt("prefixextra").build());
        List<String> matching = options.getMatchingOptions("prefix");
        assertEquals(1, matching.size());
        assertTrue(matching.contains("prefix"));
    }
