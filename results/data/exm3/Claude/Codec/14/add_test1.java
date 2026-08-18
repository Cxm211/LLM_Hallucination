// org/apache/commons/codec/language/bm/PhoneticEngineRegressionTest.java
@Test
    public void testFinalRulesEmptyCase() {
        // Test with empty final rules scenario
        Map<String, String> args = new TreeMap<String, String>();
        args.put("nameType", "SEPHARDIC");
        args.put("ruleType", "EXACT");

        String result = encode(args, true, "david");
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }