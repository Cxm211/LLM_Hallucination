public static JsonMappingException wrapWithPath(Throwable src, Reference ref)
{
    JsonMappingException jme;
    if (src instanceof JsonMappingException) {
        jme = (JsonMappingException) src;
    } else {
        String msg = src.getMessage();
        if (msg == null || msg.length() == 0) {
            msg = "(was "+src.getClass().getName()+")";
        }
        Closeable proc = null;
        if (src instanceof JsonProcessingException) {
            Object proc0 = ((JsonProcessingException) src).getProcessor();
            if (proc0 instanceof Closeable) {
                proc = (Closeable) proc0;
            }
        }
        jme = new JsonMappingException(proc, msg, src);
    }
    jme.prependPath(ref);
    return jme;
}