public DocumentType(String name, String publicId, String systemId, String baseUri) {
    super(baseUri);

    attr(NAME, name);
    if (publicId != null)
        attr(PUBLIC_ID, publicId);
    if (systemId != null)
        attr(SYSTEM_ID, systemId);
}