public JsonLocation getCurrentLocation()
    {
        final Object src = _ioContext.getSourceReference();
        return new JsonLocation(src,
                -1L, getTokenCharacterOffset(),
                getTokenLineNr(),
                getTokenColumnNr());
    }