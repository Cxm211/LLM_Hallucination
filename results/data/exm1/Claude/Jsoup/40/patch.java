public DocumentType(String name, String publicId, String systemId, String baseUri) {
    super(baseUri);

    Validate.notEmpty(name);
    attr("name", name);
    attr("publicId", publicId == null ? "" : publicId);
    attr("systemId", systemId == null ? "" : systemId);
}