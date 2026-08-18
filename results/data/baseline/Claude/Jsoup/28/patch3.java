Character consumeCharacterReference(Character additionalAllowedCharacter, boolean inAttribute) {
        if (reader.isEmpty())
            return null;
        if (additionalAllowedCharacter != null && additionalAllowedCharacter == reader.current())
            return null;
        if (reader.matchesAny('\t', '\n', '\r', '\f', ' ', '<', '&'))
            return null;

        reader.mark();
        if (reader.matchConsume("#")) {
            boolean isHexMode = reader.matchConsumeIgnoreCase("X");
            String numRef = isHexMode ? reader.consumeHexSequence() : reader.consumeDigitSequence();
            if (numRef.length() == 0) {
                characterReferenceError("numeric reference with no numerals");
                reader.rewindToMark();
                return null;
            }
            if (!reader.matchConsume(";"))
                characterReferenceError("missing semicolon");
            int charval = -1;
            try {
                int base = isHexMode ? 16 : 10;
                charval = Integer.valueOf(numRef, base);
            } catch (NumberFormatException e) {
            }
            if (charval == -1 || (charval >= 0xD800 && charval <= 0xDFFF) || charval > 0x10FFFF) {
                characterReferenceError("character outside of valid range");
                return replacementChar;
            } else {
                return (char) charval;
            }
        } else {
            String nameRef = reader.consumeLetterThenDigitSequence();
            String origNameRef = new String(nameRef);
            boolean looksLegit = reader.matches(';');
            boolean found = false;
            while (nameRef.length() > 0 && !found) {
                if (Entities.isNamedEntity(nameRef))
                    found = true;
                else {
                    nameRef = nameRef.substring(0, nameRef.length()-1);
                    reader.unconsume();
                }
            }

            if (!found) {
                reader.rewindToMark();
                if (looksLegit)
                    characterReferenceError(String.format("invalid named referenece '%s'", origNameRef));
                return null;
            }
            if (inAttribute && (reader.matchesLetter() || reader.matchesDigit() || reader.matchesAny('=', '-', '_'))) {
                reader.rewindToMark();
                return null;
            }
            if (!reader.matchConsume(";"))
                characterReferenceError("missing semicolon");
            return Entities.getCharacterByName(nameRef);
        }
    }