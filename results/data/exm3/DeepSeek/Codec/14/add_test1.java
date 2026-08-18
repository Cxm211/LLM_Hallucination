// org/apache/commons/codec/language/bm/PhoneticEngineRegressionTest.java
@Test
    public void testApplyFinalRulesMergesLanguages() throws Exception {
        Set<String> lang1 = new HashSet<String>();
        lang1.add("english");
        Set<String> lang2 = new HashSet<String>();
        lang2.add("german");
        LanguageSet ls1 = Languages.getInstance(lang1);
        LanguageSet ls2 = Languages.getInstance(lang2);
        Rule.Phoneme p1 = new Rule.Phoneme("a", ls1);
        Rule.Phoneme p2 = new Rule.Phoneme("a", ls2);
        List<Rule.Phoneme> phonemeList = Arrays.asList(p1, p2);
        PhonemeBuilder builder = new PhonemeBuilder(phonemeList);
        
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        Method method = PhoneticEngine.class.getDeclaredMethod("applyFinalRules", PhonemeBuilder.class, Map.class);
        method.setAccessible(true);
        
        Map<String, List<Rule>> finalRules = new HashMap<String, List<Rule>>();
        finalRules.put("dummy", Collections.<Rule>emptyList());
        PhonemeBuilder result = (PhonemeBuilder) method.invoke(engine, builder, finalRules);
        
        Set<Rule.Phoneme> resultPhonemes = result.getPhonemes();
        assertEquals(1, resultPhonemes.size());
        Rule.Phoneme merged = resultPhonemes.iterator().next();
        assertEquals("a", merged.getPhonemeText().toString());
        LanguageSet mergedLanguages = merged.getLanguages();
        assertTrue(mergedLanguages.contains("english"));
        assertTrue(mergedLanguages.contains("german"));
    }
