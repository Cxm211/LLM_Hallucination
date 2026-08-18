    public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        attr(NAME, name);
        if (publicId != null && !publicId.isEmpty()) {
            attr(PUBLIC_ID, publicId);
        }
        if (systemId != null && !systemId.isEmpty()) {
            attr(SYSTEM_ID, systemId);
        }
    }