protected IOException _throwAsIOE(JsonParser p, Exception e) throws IOException
    {
        ClassUtil.throwIfIOE(e);
        ClassUtil.throwIfRTE(e);
        
        Throwable th = ClassUtil.getRootCause(e);
        throw JsonMappingException.from(p, ClassUtil.exceptionMessage(th), th);
    }