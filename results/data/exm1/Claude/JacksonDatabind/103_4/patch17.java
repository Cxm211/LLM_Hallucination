protected JsonMappingException wrapException(Throwable t)
    {
        for (Throwable curr = t; curr != null; curr = curr.getCause()) {
            if (curr instanceof JsonMappingException) {
                return (JsonMappingException) curr;
            }
        }
        String msg = t.getMessage();
        return new JsonMappingException(null,
                "Instantiation of "+getValueTypeDesc()+" value failed: "+((msg == null) ? "N/A" : msg), t);
    }