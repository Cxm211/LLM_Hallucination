public void writeBoolean(boolean state) throws IOException {
        _append(state ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE, state ? Boolean.TRUE : Boolean.FALSE);
    }