public void writeEmbeddedObject(Object object) throws IOException {
        if (object instanceof byte[]) {
            writeBinary((byte[]) object);
        } else if (object instanceof Object[]) {
            writeStartArray();
            for (Object elem : (Object[]) object) {
                writeObject(elem);
            }
            writeEndArray();
        } else {
            throw new JsonGenerationException("No native support for writing embedded objects",
                    this);
        }
    }