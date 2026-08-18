    public <T> MappingIterator<T> readValues(byte[] src, int offset, int length)
        throws IOException, JsonProcessingException
    {
        if (_dataFormatReaders != null) {
            Object format = _dataFormatReaders.findFormat(src, offset, length);
            if (format != null) {
                return _detectBindAndReadValues(format, false);
            }
        }
        return _bindAndReadValues(_considerFilter(_parserFactory.createParser(src), true));
    }