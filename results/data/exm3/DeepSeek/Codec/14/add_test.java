// org/apache/commons/codec/language/bm/PhoneticEngineRegressionTest.java
@Test
    public void testNoRuleMatches() {
        Map<String, String> args = new TreeMap<String, String>();
        args.put("nameType", "GENERIC");
        args.put("ruleType", "APPROX");
        assertEquals(encode(args, true, "xyz"), "xyz");
    }
