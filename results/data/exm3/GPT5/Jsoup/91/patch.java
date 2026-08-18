public UncheckedIOException(IOException cause) {
        super(cause == null ? null : cause.getMessage(), cause);
    }