    private PhonemeBuilder applyFinalRules(final PhonemeBuilder phonemeBuilder,
                                           final Map<String, List<Rule>> finalRules) {
        if (finalRules == null) {
            throw new NullPointerException("finalRules can not be null");
        }
        if (finalRules.isEmpty()) {
            return phonemeBuilder;
        }

        // Collect phonemes by their text and merge language sets for identical texts
        final Map<String, Languages.LanguageSet> mergedByText = new HashMap<String, Languages.LanguageSet>();

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

            for (final Rule.Phoneme p : subBuilder.getPhonemes()) {
                final String txt = p.getPhonemeText().toString();
                final Languages.LanguageSet existing = mergedByText.get(txt);
                if (existing == null) {
                    mergedByText.put(txt, p.getLanguages());
                } else {
                    mergedByText.put(txt, existing.merge(p.getLanguages()));
                }
            }
        }

        final Set<Rule.Phoneme> phonemes = new TreeSet<Rule.Phoneme>(Rule.Phoneme.COMPARATOR);
        for (final Map.Entry<String, Languages.LanguageSet> e : mergedByText.entrySet()) {
            phonemes.add(new Rule.Phoneme(e.getKey(), e.getValue()));
        }

        return new PhonemeBuilder(phonemes);
    }