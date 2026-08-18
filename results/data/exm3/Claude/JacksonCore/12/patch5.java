public JsonLocation getTokenLocation()
{
    final Object src = _ioContext.getSourceReference();
    if (_currToken == JsonToken.FIELD_NAME) {
        long total = -1L;
        if (this instanceof com.fasterxml.jackson.core.json.UTF8StreamJsonParser) {
            total = ((com.fasterxml.jackson.core.json.UTF8StreamJsonParser)this)._currInputProcessed + _tokenInputTotal;
        } else {
            total = _tokenInputTotal;
        }
        return new JsonLocation(src,
                total, -1L, _tokenInputRow, _tokenInputCol);
    }
    return new JsonLocation(src,
            getTokenCharacterOffset(), -1L, getTokenLineNr(),
            getTokenColumnNr());
}