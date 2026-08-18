    private static final Set<String> BASE_ENTITIES = new HashSet<String>();
    static {
        BASE_ENTITIES.add("amp");
        BASE_ENTITIES.add("quot");
        BASE_ENTITIES.add("reg");
        BASE_ENTITIES.add("lt");
        BASE_ENTITIES.add("gt");
        BASE_ENTITIES.add("apos");
    }
    Character consumeCharacterReference(Character additionalAllowedCharacter, boolean inAttribute) {
        if (reader.isEmpty())
            return null;
        if (additionalAllowedCharacter != null && additionalAllowedCharacter == reader.current())
            return null;
        if (reader.matchesAny('\t', '\n', '\r', '\f', ' ', '<', '&'))
            return null;

        reader.mark();
        if (reader.matchConsume("#")) { // numbered
            boolean isHexMode = reader.matchConsumeIgnoreCase("X");
            String numRef = isHexMode ? reader.consumeHexSequence() : reader.consumeDigitSequence();
            if (numRef.length() == 0) { // didn't match anything
                characterReferenceError("numeric reference with no numerals");
                reader.rewindToMark();
                return null;
            }
            if (!reader.matchConsume(";"))
                characterReferenceError("missing semicolon"); // missing semi
            int charval = -1;
            try {
                int base = isHexMode ? 16 : 10;
                charval = Integer.valueOf(numRef, base);
            } catch (NumberFormatException e) {
            } // skip
            if (charval == -1 || (charval >= 0xD800 && charval <= 0xDFFF) || charval > 0x10FFFF) {
                characterReferenceError("character outside of valid range");
                return replacementChar;
            } else {
                // todo: implement number replacement table
                // todo: check for extra illegal unicode points as parse errors
                return (char) charval;
            }
        } else { // named
            String nameRef = reader.consumeLetterThenDigitSequence();
            String origNameRef = new String(nameRef);
            boolean looksLegit = reader.matches(';');
            boolean found = Entities.isNamedEntity(nameRef);
            
            if (!found) {
                reader.rewindToMark();
                if (looksLegit) // named with semicolon
                    characterReferenceError(String.format("invalid named reference '%s'", origNameRef));
                return null;
            }
            // If no semicolon, check if it is a base entity
            if (!looksLegit && !BASE_ENTITIES.contains(nameRef)) {
                reader.rewindToMark();
                return null;
            }
            if (inAttribute && (reader.matchesLetter() || reader.matchesDigit() || reader.matchesAny('=', '-', '_'))) {
                // don't want that to match
                reader.rewindToMark();
                return null;
            }
            if (looksLegit) {
                if (!reader.matchConsume(";"))
                    characterReferenceError("missing semicolon");
            }
            return Entities.getCharacterByName(nameRef);
        }
    }