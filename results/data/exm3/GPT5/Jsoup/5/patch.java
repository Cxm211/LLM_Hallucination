private Attribute parseAttribute() {
        tq.consumeWhitespace();
        String key = tq.consumeAttributeKey();
        String value = "";
        tq.consumeWhitespace();
        if (tq.matchChomp("=")) {
            tq.consumeWhitespace();

            if (tq.matchChomp(SQ)) {
                value = tq.chompTo(SQ);
                // consume the closing single quote if present
                tq.matchChomp(SQ);
            } else if (tq.matchChomp(DQ)) {
                value = tq.chompTo(DQ);
                // consume the closing double quote if present
                tq.matchChomp(DQ);
            } else {
                StringBuilder valueAccum = new StringBuilder();
                // no ' or " to look for, so scan to end tag or space (or end of stream)
                while (!tq.matchesAny("<", "/>", ">") && !tq.matchesWhitespace() && !tq.isEmpty()) {
                    valueAccum.append(tq.consume());
                }
                value = valueAccum.toString();
            }
            tq.consumeWhitespace();
        }
        if (key.length() != 0)
            return Attribute.createFromEncoded(key, value);
        else {
            // If no key, do not consume tag end markers; otherwise consume one char to avoid stalling.
            if (!tq.isEmpty() && !tq.matchesAny(">", "/>")) {
                tq.consume();
            }
            return null;
        }
    }