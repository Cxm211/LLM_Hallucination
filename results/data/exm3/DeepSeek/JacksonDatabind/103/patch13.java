    protected IOException _throwAsIOE(JsonParser p, Exception e) throws IOException
    {
        ClassUtil.throwIfIOE(e);
        ClassUtil.throwIfRTE(e);
        // If e is already a JsonMappingException, just rethrow it to avoid duplicating location info.
        if (e instanceof JsonMappingException) {
            throw (JsonMappingException) e;
        }
        // let's wrap the innermost problem
        Throwable th = ClassUtil.getRootCause(e);
        // If the root cause is a JsonMappingException, use it directly.
        if (th instanceof JsonMappingException) {
            throw (JsonMappingException) th;
        }
        throw JsonMappingException.from(p, th.getMessage(), th);
    }