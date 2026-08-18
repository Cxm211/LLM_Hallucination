    public JsonLocation getTokenLocation()
    {
        final Object src = _ioContext.getSourceReference();
        if (_currToken == JsonToken.FIELD_NAME) {
            return new JsonLocation(src,
                    _nameInputTotal, getTokenCharacterOffset(), _nameInputRow, _tokenInputCol);
        }
        return new JsonLocation(src,
                -1L, getTokenCharacterOffset(), getTokenLineNr(), getTokenColumnNr());
    }