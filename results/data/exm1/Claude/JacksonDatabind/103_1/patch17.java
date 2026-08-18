protected JsonMappingException wrapException(Throwable t)
    {
        for (Throwable curr = t; curr != null; curr = curr.getCause()) {
            if (curr instanceof JsonMappingException) {
                return (JsonMappingException) curr;
            }
        }
        return new JsonMappingException(null,
                "Instantiation of "+getValueTypeDesc()+" value failed: "+t.getMessage(), t);
    }