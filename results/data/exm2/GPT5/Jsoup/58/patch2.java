public boolean isValid(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);
        // if the input document had parse errors, it's not valid as body HTML
        try {
            Parser parser = dirtyDocument.parser();
            if (parser != null && parser.getErrors() != null && parser.getErrors().size() > 0)
                return false;
        } catch (Throwable t) {
            // ignore: parser info not available
        }

        Document clean = Document.createShell(dirtyDocument.baseUri());
        int numDiscarded = copySafeNodes(dirtyDocument.body(), clean.body());
        return numDiscarded == 0;
    }