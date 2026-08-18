CharacterReader(String input) {
        Validate.notNull(input);

        this.input = input;
        this.length = input.length();
        this.pos = 0;
    }