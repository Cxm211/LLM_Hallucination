private JsonToken _nextBuffered(TokenFilterContext buffRoot) throws IOException
    {
        _exposedContext = buffRoot;
        TokenFilterContext ctxt = buffRoot;
        JsonToken t = ctxt.nextTokenToRead();
        if (t != null) {
            return t;
        }
        while (true) {
            if (ctxt == _headContext) {
                throw _constructError("Internal error: failed to locate expected buffered tokens");
            }
            ctxt = _exposedContext.findChildOf(ctxt);
            _exposedContext = ctxt;
            if (ctxt == null) {
                throw _constructError("Unexpected problem: chain of filtered context broken");
            }
            t = _exposedContext.nextTokenToRead();
            if (t != null) {
                return t;
            }
        }
    }