public UncheckedIOException(IOException cause) {
        if (cause == null) throw new NullPointerException();
        super(cause.getMessage(), cause);
    }