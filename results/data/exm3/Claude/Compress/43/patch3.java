private boolean usesDataDescriptor(final int zipMethod, final boolean phased) {
        return !phased && zipMethod == DEFLATED && channel == null;
    }