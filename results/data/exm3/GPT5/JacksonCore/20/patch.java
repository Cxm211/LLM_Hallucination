public void writeEmbeddedObject(Object object) throws IOException {
        // Handle nulls as JSON null
        if (object == null) {
            writeNull();
            return;
        }
        // Handle raw binary data as Base64-encoded JSON String
        if (object instanceof byte[]) {
            writeBinary((byte[]) object);
            return;
        }
        // Otherwise, no native support
        throw new JsonGenerationException("No native support for writing embedded objects",
                this);
    }