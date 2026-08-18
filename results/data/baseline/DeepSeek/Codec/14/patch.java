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

        // Merge phonemes with same text but different language sets
        for (final Rule.Phoneme subPhoneme : subBuilder.getPhonemes()) {
            Rule.Phoneme existing = null;
            for (final Rule.Phoneme p : phonemes) {
                if (Rule.Phoneme.COMPARATOR.compare(subPhoneme, p) == 0) {
                    existing = p;
                    break;
                }
            }
            if (existing == null) {
                phonemes.add(subPhoneme);
            } else {
                phonemes.remove(existing);
                phonemes.add(subPhoneme.mergeLanguages(existing));
            }
        }
    }

    return new PhonemeBuilder(phonemes);
}