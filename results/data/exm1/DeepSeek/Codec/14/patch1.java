        public Phoneme join(final Phoneme right) {
            return new Phoneme(this.phonemeText.toString() + right.phonemeText.toString(),
                               this.languages.restrictTo(right.languages));
        }