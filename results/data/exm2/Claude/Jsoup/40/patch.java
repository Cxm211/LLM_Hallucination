public DocumentType(String name, String publicId, String systemId, String baseUri) {
    super(baseUri);

    Validate.notEmpty(name);
    Validate.notNull(publicId);
    Validate.notNull(systemId);
    attr("name", name);
    attr("publicId", publicId);
    attr("systemId", systemId);
}