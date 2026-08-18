    public void writeNumber(String encodedValue) throws IOException {
        /* 03-Dec-2010, tatu: related to [JACKSON-423], should try to keep as numeric
         *   identity as long as possible
         */
        writeNumber(new BigDecimal(encodedValue));
    }