private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeInt(size());
    out.writeInt(buffer.length);
    for (Iterator it = iterator(); it.hasNext();) {
        out.writeObject(it.next());
    }
}