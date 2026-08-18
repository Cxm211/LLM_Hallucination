private boolean usesDataDescriptor(final int zipMethod, final boolean phased) {
    return zipMethod == DEFLATED && channel == null && !phased;
}