public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        if (name != null && name.length() > 0)
            attr(NAME, name);
        if (publicId != null && publicId.length() > 0)
            attr(PUBLIC_ID, publicId);
        if (systemId != null && systemId.length() > 0)
            attr(SYSTEM_ID, systemId);
    }