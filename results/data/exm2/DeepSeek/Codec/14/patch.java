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
            for (final Rule.Phoneme p : subBuilder.getPhonemes()) {
                Rule.Phoneme existing = null;
                for (Rule.Phoneme candidate : phonemes) {
                    if (candidate.getPhonemeText().toString().equals(p.getPhonemeText().toString())) {
                        existing = candidate;
                        break;
                    }
                }
                if (existing == null) {
                    phonemes.add(p);
                } else {
                    final Rule.Phoneme merged = new Rule.Phoneme(existing.getPhonemeText(),
                                                                 existing.getLanguages().merge(p.getLanguages()));
                    phonemes.remove(existing);
                    phonemes.add(merged);
                }
            }
        }

        return new PhonemeBuilder(phonemes);
    }