private PhonemeBuilder applyFinalRules(final PhonemeBuilder phonemeBuilder,
                                           final Map<String, List<Rule>> finalRules) {
        if (finalRules == null) {
            throw new NullPointerException("finalRules can not be null");
        }
        if (finalRules.isEmpty()) {
            return phonemeBuilder;
        }

        final Map<CharSequence, Set<Rule.Phoneme>> phonemesByText = new LinkedHashMap<CharSequence, Set<Rule.Phoneme>>();

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
                final CharSequence newText = newPhoneme.getPhonemeText();
                Set<Rule.Phoneme> existingPhonemes = phonemesByText.get(newText);
                if (existingPhonemes == null) {
                    existingPhonemes = new LinkedHashSet<Rule.Phoneme>();
                    phonemesByText.put(newText, existingPhonemes);
                }
                existingPhonemes.add(newPhoneme);
            }
        }

        final Set<Rule.Phoneme> mergedPhonemes = new TreeSet<Rule.Phoneme>(Rule.Phoneme.COMPARATOR);
        for (final Set<Rule.Phoneme> phonemeSet : phonemesByText.values()) {
            if (phonemeSet.size() == 1) {
                mergedPhonemes.addAll(phonemeSet);
            } else {
                Rule.Phoneme merged = null;
                for (final Rule.Phoneme phoneme : phonemeSet) {
                    if (merged == null) {
                        merged = phoneme;
                    } else {
                        merged = merged.mergeWithLanguage(phoneme.getLanguages());
                    }
                }
                mergedPhonemes.add(merged);
            }
        }

        return new PhonemeBuilder(mergedPhonemes);
    }