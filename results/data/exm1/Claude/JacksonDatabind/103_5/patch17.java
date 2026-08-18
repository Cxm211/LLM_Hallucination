protected JsonMappingException wrapException(Throwable t)
    {
        // 05-Nov-2015, tatu: This used to always unwrap the whole exception, but now only
        //   does so if and until `JsonMappingException` is found.
        for (Throwable curr = t; curr != null; curr = curr.getCause()) {
            if (curr instanceof JsonMappingException) {
                return (JsonMappingException) curr;
            }
        }
        String msg = t.getMessage();
        return new JsonMappingException(null,
                "Instantiation of "+getValueTypeDesc()+" value failed: "+(msg == null ? "N/A" : msg), t);
    }