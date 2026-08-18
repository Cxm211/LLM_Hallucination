private void normalise(Element element) {
        List<TextNode> toMove = new ArrayList<TextNode>();
        for (Node node : element.childNodes) {
            if (node instanceof TextNode) {
                TextNode tn = (TextNode) node;
                if (!tn.isBlank())
                    toMove.add(tn);
            }
        }
        if (toMove.isEmpty())
            return;

        boolean needSpaceAfter = body().childNodeSize() > 0;
        for (int i = toMove.size() - 1; i >= 0; i--) {
            TextNode tn = toMove.get(i);
            element.removeChild(tn);
            if (needSpaceAfter)
                body().prependChild(new TextNode(" ", ""));
            body().prependChild(tn);
            needSpaceAfter = true;
        }
    }