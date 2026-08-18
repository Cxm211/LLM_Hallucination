public String absUrl(String attributeKey) {
        Validate.notEmpty(attributeKey);

        String relUrl = attr(attributeKey);
        if (!hasAttr(attributeKey)) {
            return ""; // nothing to make absolute with
        } else {
            URL base;
            try {
                try {
                    base = new URL(baseUri);
                } catch (MalformedURLException e) {
                    // the base is unsuitable, but the attribute may be abs on its own, so try that
                    URL abs = new URL(relUrl);
                    return abs.toExternalForm();
                }
                // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
                if (relUrl.startsWith("?")) {
                    String basePath = base.getPath();
                    if (basePath != null && basePath.length() > 0) {
                        int lastSlash = basePath.lastIndexOf('/');
                        if (lastSlash != -1 && lastSlash < basePath.length() - 1) {
                            // there's a file component, preserve it
                            URL abs = new URL(base.getProtocol(), base.getHost(), base.getPort(), basePath + relUrl);
                            return abs.toExternalForm();
                        }
                    }
                }
                URL abs = new URL(base, relUrl);
                return abs.toExternalForm();
            } catch (MalformedURLException e) {
                return "";
            }
        }
    }