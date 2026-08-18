// ===== FIXED com.fasterxml.jackson.databind.ObjectReader :: readValues(byte[], int, int) [lines 1435-1443] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-57-fixed/src/main/java/com/fasterxml/jackson/databind/ObjectReader.java =====
    public <T> MappingIterator<T> readValues(byte[] src, int offset, int length)
        throws IOException, JsonProcessingException
    {
        if (_dataFormatReaders != null) {
            return _detectBindAndReadValues(_dataFormatReaders.findFormat(src, offset, length), false);
        }
        return _bindAndReadValues(_considerFilter(_parserFactory.createParser(src, offset, length),
                true));
    }
