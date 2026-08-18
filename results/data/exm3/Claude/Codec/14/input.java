// buggy function
    private PhonemeBuilder applyFinalRules(final PhonemeBuilder phonemeBuilder,
                                           final Map<String, List<Rule>> finalRules) {
        if (finalRules == null) {
            throw new NullPointerException("finalRules can not be null");
        }
        if (finalRules.isEmpty()) {
            return phonemeBuilder;
        }

        final Set<Rule.Phoneme> phonemes = new TreeSet<Rule.Phoneme>(Rule.Phoneme.COMPARATOR);

        for (final Rule.Phoneme phoneme : phonemeBuilder.getPhonemes()) {
            PhonemeBuilder subBuilder = PhonemeBuilder.empty(phoneme.getLanguages());
            final String phonemeText = phoneme.getPhonemeText().toString();

            for (int i = 0; i < phonemeText.length();) {
                final RulesApplication rulesApplication =
                        new RulesApplication(finalRules, phonemeText, subBuilder, i, maxPhonemes).invoke();
                final boolean found = rulesApplication.isFound();
                subBuilder = rulesApplication.getPhonemeBuilder();

                if (!found) {
                    // not found, appending as-is
                    subBuilder.append(phonemeText.subSequence(i, i + 1));
                }

                i = rulesApplication.getI();
            }

            // the phonemes map orders the phonemes only based on their text, but ignores the language set
            // when adding new phonemes, check for equal phonemes and merge their language set, otherwise
            // phonemes with the same text but different language set get lost
            phonemes.addAll(subBuilder.getPhonemes());
        }

        return new PhonemeBuilder(phonemes);
    }

        public Phoneme join(final Phoneme right) {
            return new Phoneme(this.phonemeText.toString() + right.phonemeText.toString(),
                               this.languages.restrictTo(right.languages));
        }

// trigger testcase
// org/apache/commons/codec/language/bm/PhoneticEngineRegressionTest.java::testCompatibilityWithOriginalVersion
@Test
    public void testCompatibilityWithOriginalVersion() {
        // see CODEC-187
        // comparison: http://stevemorse.org/census/soundex.html

        Map<String, String> args = new TreeMap<String, String>();
        args.put("nameType", "GENERIC");
        args.put("ruleType", "APPROX");

        assertEquals(encode(args, true, "abram"), "Ybram|Ybrom|abram|abran|abrom|abron|avram|avrom|obram|obran|obrom|obron|ovram|ovrom");
        assertEquals(encode(args, true, "Bendzin"), "bndzn|bntsn|bnzn|vndzn|vntsn");

        args.put("nameType", "ASHKENAZI");
        args.put("ruleType", "APPROX");

        assertEquals(encode(args, true, "abram"), "Ybram|Ybrom|abram|abrom|avram|avrom|imbram|imbrom|obram|obrom|ombram|ombrom|ovram|ovrom");
        assertEquals(encode(args, true, "Halpern"), "YlpYrn|Ylpirn|alpYrn|alpirn|olpYrn|olpirn|xalpirn|xolpirn");

    }
