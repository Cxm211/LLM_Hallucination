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

            // Merge phonemes with equal text by combining their language sets
            for (final Rule.Phoneme newPhoneme : subBuilder.getPhonemes()) {
                boolean merged = false;
                for (final Iterator<Rule.Phoneme> it = phonemes.iterator(); it.hasNext();) {
                    final Rule.Phoneme existing = it.next();
                    if (Rule.Phoneme.COMPARATOR.compare(existing, newPhoneme) == 0) {
                        final Rule.Phoneme combined = existing.mergeWithLanguage(newPhoneme.getLanguages());
                        it.remove();
                        phonemes.add(combined);
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    phonemes.add(newPhoneme);
                }
            }
        }

        return new PhonemeBuilder(phonemes);
    }