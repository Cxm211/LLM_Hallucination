static Document parseInputStream(InputStream input, String charsetName, String baseUri, Parser parser) throws IOException  {
        if (input == null)
            return new Document(baseUri);
        input = ConstrainableInputStream.wrap(input, bufferSize, 0);

        Document doc = null;
        boolean fullyRead = false;

        input.mark(bufferSize);
        ByteBuffer firstBytes = readToByteBuffer(input, firstReadBufferSize - 1);
        fullyRead = input.read() == -1;
        input.reset();

        BomCharset bomCharset = detectCharsetFromBom(firstBytes);
        if (bomCharset != null) {
            charsetName = bomCharset.charset;
            input.skip(bomCharset.offset);
        }

        if (charsetName == null) {
            String docData = Charset.forName(defaultCharset).decode(firstBytes).toString();
            doc = parser.parseInput(docData, baseUri);

            Elements metaElements = doc.select("meta[http-equiv=content-type], meta[charset]");
            String foundCharset = null;
            for (Element meta : metaElements) {
                if (meta.hasAttr("http-equiv"))
                    foundCharset = getCharsetFromContentType(meta.attr("content"));
                if (foundCharset == null && meta.hasAttr("charset"))
                    foundCharset = meta.attr("charset");
                if (foundCharset != null)
                    break;
            }

            if (foundCharset == null && doc.childNodeSize() > 0 && doc.childNode(0) instanceof XmlDeclaration) {
                XmlDeclaration prolog = (XmlDeclaration) doc.childNode(0);
                if (prolog.name().equals("xml"))
                    foundCharset = prolog.attr("encoding");
            }
            foundCharset = validateCharset(foundCharset);
            if (foundCharset != null && !foundCharset.equalsIgnoreCase(defaultCharset)) {
                charsetName = foundCharset.trim().replaceAll("[\"']", "");
                doc = null;
            } else if (!fullyRead) {
                doc = null;
            }
        } else {
            Validate.notEmpty(charsetName, "Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
        }
        if (doc == null) {
            if (charsetName == null)
                charsetName = defaultCharset;
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, charsetName), bufferSize);
                doc = parser.parseInput(reader, baseUri);
            doc.outputSettings().charset(charsetName);
        }
        input.close();
        return doc;
    }