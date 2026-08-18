public UncheckedIOException(IOException cause) {
        super(cause != null ? cause.getMessage() : null, cause);
    }