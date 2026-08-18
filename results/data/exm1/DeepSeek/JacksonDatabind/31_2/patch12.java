public void writeNumber(String encodedValue) throws IOException {
        /* 03-Dec-2010, tatu: related to [JACKSON-423], should try to keep as numeric
         *   identity as long as possible
         */
        boolean isFloat = encodedValue.indexOf('.') >= 0
                || encodedValue.indexOf('e') >= 0
                || encodedValue.indexOf('E') >= 0;
        _append(isFloat ? JsonToken.VALUE_NUMBER_FLOAT : JsonToken.VALUE_NUMBER_INT, encodedValue);
    }