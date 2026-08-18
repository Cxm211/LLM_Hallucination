private PhonemeBuilder applyFinalRules(final PhonemeBuilder phonemeBuilder,
                                           final Map<String, List<Rule>> finalRules) {
        if (finalRules == null) {
            throw new NullPointerException("finalRules can not be null");
        }
        if (finalRules.isEmpty()) {
            return phonemeBuilder;
        }

        final Map<CharSequence, Rule.Phoneme> phonemeMap = new LinkedHashMap<CharSequence, Rule.Phoneme>();

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
            for (final Rule.Phoneme newPhoneme : subBuilder.getPhonemes()) {
                final CharSequence newPhonemeText = newPhoneme.getPhonemeText();
                if (phonemeMap.containsKey(newPhonemeText)) {
                    final Rule.Phoneme existingPhoneme = phonemeMap.get(newPhonemeText);
                    final Rule.Phoneme mergedPhoneme = existingPhoneme.join(newPhoneme);
                    phonemeMap.put(newPhonemeText, mergedPhoneme);
                } else {
                    phonemeMap.put(newPhonemeText, newPhoneme);
                }
            }
        }

        return new PhonemeBuilder(new TreeSet<Rule.Phoneme>(phonemeMap.values()));
    }