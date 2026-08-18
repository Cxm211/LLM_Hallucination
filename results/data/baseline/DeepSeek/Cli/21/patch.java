    public WriteableCommandLineImpl(final Option rootOption,
                                    final List arguments) {
        this.prefixes = rootOption != null ? rootOption.getPrefixes() : new java.util.ArrayList<>();
        this.normalised = arguments;
    }