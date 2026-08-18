protected IOException _throwAsIOE(JsonParser p, Exception e) throws IOException
    {
        ClassUtil.throwIfIOE(e);
        ClassUtil.throwIfRTE(e);
        // let's wrap the innermost problem
        Throwable th = ClassUtil.getRootCause(e);
        String msg = th.getMessage();
        throw JsonMappingException.from(p, msg == null ? "N/A" : msg, th);
    }