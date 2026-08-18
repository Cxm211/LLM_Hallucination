    public void writeNumber(String encodedValue) throws IOException {
        if (encodedValue == null) {
            writeNull();
            return;
        }
        /* 03-Dec-2010, tatu: related to [JACKSON-423], should try to keep as numeric
         *   identity as long as possible
         */
        _append(JsonToken.VALUE_NUMBER_FLOAT, encodedValue);
    }