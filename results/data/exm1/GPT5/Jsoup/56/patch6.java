void read(Tokeniser t, CharacterReader r) {
            if (r.isEmpty()) {
                t.eofError(this);
                t.doctypePending.forceQuirks = true;
                t.emitDoctypePending();
                t.transition(Data);
                return;
            }
            if (r.matchesAny('\t', '\n', '\r', '\f', ' ')) {
                r.advance(); // ignore whitespace
            } else if (r.matches('>')) {
                t.emitDoctypePending();
                t.advanceTransition(Data);
            } else if (r.matchConsumeIgnoreCase(DocumentType.PUBLIC_KEY)) {
                t.transition(AfterDoctypePublicKeyword);
            } else if (r.matchConsumeIgnoreCase(DocumentType.SYSTEM_KEY)) {
                t.transition(AfterDoctypeSystemKeyword);
            } else {
                // read the doctype name
                String name = r.consumeToAny('\t', '\n', '\r', '\f', ' ', '>');
                if (t.doctypePending.name == null)
                    t.doctypePending.name = new StringBuilder();
                t.doctypePending.name.append(name);
                t.transition(AfterDoctypeName);
            }

        }