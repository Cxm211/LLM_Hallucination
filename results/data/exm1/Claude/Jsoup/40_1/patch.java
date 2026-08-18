public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        Validate.notEmpty(name);
        if (attributes == null) {
            attributes = new Attributes();
        }
        attr("name", name);
        attr("publicId", publicId);
        attr("systemId", systemId);
    }