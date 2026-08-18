    public void writeEmbeddedObject(Object object) throws IOException {
        // 01-Sep-2016, tatu: As per [core#318], handle small number of cases
        if (object == null) {
            writeNull();
        } else if (object instanceof byte[]) {
            writeBinary((byte[]) object);
        } else {
            throw new JsonGenerationException("No native support for writing embedded objects",
                    this);
        }
    }