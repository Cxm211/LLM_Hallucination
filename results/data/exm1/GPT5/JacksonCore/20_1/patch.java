public void writeEmbeddedObject(Object object) throws IOException {
        // Handle common embedded object types per [core#318]
        if (object == null) {
            writeNull();
            return;
        }
        if (object instanceof byte[]) {
            writeBinary((byte[]) object);
            return;
        }
        if (object instanceof com.fasterxml.jackson.core.io.SerializedString) {
            writeString((com.fasterxml.jackson.core.io.SerializedString) object);
            return;
        }
        if (object instanceof com.fasterxml.jackson.core.SerializableString) {
            writeString((com.fasterxml.jackson.core.SerializableString) object);
            return;
        }
        if (object instanceof com.fasterxml.jackson.core.util.RawValue) {
            ((com.fasterxml.jackson.core.util.RawValue) object).serialize(this);
            return;
        }
        throw new JsonGenerationException("No native support for writing embedded objects", this);
    }