    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(size());
        for (Iterator it = iterator(); it.hasNext();) {
            out.writeObject(it.next());
        }
    }