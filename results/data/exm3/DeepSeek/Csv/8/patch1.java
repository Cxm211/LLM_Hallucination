    void validate() throws IllegalStateException {
        if (quoteChar != null && delimiter == quoteChar.charValue()) {
            throw new IllegalStateException(
                    "The quoteChar character and the delimiter cannot be the same ('" + quoteChar + "')");
        }

        if (escape != null && delimiter == escape.charValue()) {
            throw new IllegalStateException(
                    "The escape character and the delimiter cannot be the same ('" + escape + "')");
        }

        if (commentStart != null && delimiter == commentStart.charValue()) {
            throw new IllegalStateException(
                    "The comment start character and the delimiter cannot be the same ('" + commentStart + "')");
        }

        if (quoteChar != null && quoteChar.equals(commentStart)) {
            throw new IllegalStateException(
                    "The comment start character and the quoteChar cannot be the same ('" + commentStart + "')");
        }

        if (escape != null && escape.equals(commentStart)) {
            throw new IllegalStateException(
                    "The comment start and the escape character cannot be the same ('" + commentStart + "')");
        }

        // Validate duplicate header names (case‑insensitive)
        if (header != null) {
            final Set<String> normalized = new HashSet<>();
            for (String h : header) {
                final String key = (h == null) ? null : h.toLowerCase(Locale.ENGLISH);
                if (!normalized.add(key)) {
                    throw new IllegalStateException("The header contains duplicate names: " + Arrays.toString(header));
                }
            }
        }

        if (escape == null && quotePolicy == Quote.NONE) {
            throw new IllegalStateException("No quotes mode set but no escape character is set");
        }
    }