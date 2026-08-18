    public JsonLocation getTokenLocation()
    {
        final Object src = _ioContext.getSourceReference();
        if (_currToken == JsonToken.FIELD_NAME) {
            return new JsonLocation(src,
                    -1L, _nameInputTotal, _nameInputRow, _nameInputCol);
        }
        return new JsonLocation(src,
                -1L, getTokenCharacterOffset(), getTokenLineNr(),
                getTokenColumnNr());
    }