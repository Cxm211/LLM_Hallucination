public static JsonMappingException wrapWithPath(Throwable src, Reference ref)
    {
        JsonMappingException jme;
        if (src instanceof JsonMappingException) {
            jme = (JsonMappingException) src;
        } else {
            // [databind#2128]: try to avoid duplication
            String msg;
            // 17-Aug-2015, tatu: Let's also pass the processor (parser/generator) along
            Closeable proc = null;
            if (src instanceof JsonProcessingException) {
                JsonProcessingException jpe = (JsonProcessingException) src;
                // Use original message to avoid location duplication when we add it again
                msg = jpe.getOriginalMessage();
                Object proc0 = jpe.getProcessor();
                if (proc0 instanceof Closeable) {
                    proc = (Closeable) proc0;
                }
            } else {
                msg = src.getMessage();
            }
            // Let's use a more meaningful placeholder if all we have is null
            if (msg == null || msg.length() == 0) {
                msg = "(was "+src.getClass().getName()+")";
            }
            jme = new JsonMappingException(proc, msg, src);
        }
        jme.prependPath(ref);
        return jme;
    }