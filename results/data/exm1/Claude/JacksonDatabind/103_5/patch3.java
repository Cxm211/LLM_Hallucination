public static JsonMappingException fromUnexpectedIOE(IOException src) {
        String msg = src.getMessage();
        return new JsonMappingException(null,
                String.format("Unexpected IOException (of type %s): %s",
                        src.getClass().getName(),
                        msg == null ? "N/A" : msg));
    }