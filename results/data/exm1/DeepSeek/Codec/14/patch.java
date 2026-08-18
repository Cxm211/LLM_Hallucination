    private PhonemeBuilder applyFinalRules(final PhonemeBuilder phonemeBuilder,
                                           final Map<String, List<Rule>> finalRules) {
        if (finalRules == null) {
            throw new NullPointerException("finalRules can not be null");
        }
        if (finalRules.isEmpty()) {
            return phonemeBuilder;
        }

        final Map<String, Rule.Phoneme> mergedPhonemes = new LinkedHashMap<String, Rule.Phoneme>();

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
                final String text = p.getPhonemeText().toString();
                if (mergedPhonemes.containsKey(text)) {
                    final Rule.Phoneme existing = mergedPhonemes.get(text);
                    mergedPhonemes.put(text, new Rule.Phoneme(existing.getPhonemeText(), existing.getLanguages().merge(p.getLanguages())));
                } else {
                    mergedPhonemes.put(text, p);
                }
            }
        }

        final Set<Rule.Phoneme> phonemes = new TreeSet<Rule.Phoneme>(Rule.Phoneme.COMPARATOR);
        phonemes.addAll(mergedPhonemes.values());

        return new PhonemeBuilder(phonemes);
    }