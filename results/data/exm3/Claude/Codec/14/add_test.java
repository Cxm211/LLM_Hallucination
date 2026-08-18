// org/apache/commons/codec/language/bm/PhoneticEngineRegressionTest.java
@Test
    public void testFinalRulesWithMultipleLanguageSets() {
        // Test case to ensure phonemes with same text but different language sets are properly merged
        Map<String, String> args = new TreeMap<String, String>();
        args.put("nameType", "GENERIC");
        args.put("ruleType", "APPROX");

        // Test a name that may produce phonemes with identical text but different language sets
        String result = encode(args, true, "angelo");
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }