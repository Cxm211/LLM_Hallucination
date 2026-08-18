public String absUrl(String attributeKey) {
    Validate.notEmpty(attributeKey);
    String relUrl = attr(attributeKey);
    if (!hasAttr(attributeKey)) {
        return "";
    } else {
        try {
            try {
                URL base = new URL(baseUri);
                URI baseUriObj = base.toURI();
                URI resolved = baseUriObj.resolve(relUrl);
                return resolved.toURL().toExternalForm();
            } catch (MalformedURLException e) {
                URL abs = new URL(relUrl);
                return abs.toExternalForm();
            } catch (URISyntaxException e) {
                URL abs = new URL(relUrl);
                return abs.toExternalForm();
            }
        } catch (MalformedURLException e) {
            return "";
        }
    }
}