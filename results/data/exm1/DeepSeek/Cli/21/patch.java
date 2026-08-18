public WriteableCommandLineImpl(final Option rootOption,
                                    final List arguments) {
    this.prefixes = rootOption.getPrefixes();
    if (this.prefixes == null) {
        this.prefixes = Collections.EMPTY_SET;
    }
    this.normalised = arguments;
}