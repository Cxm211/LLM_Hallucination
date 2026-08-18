public ZipShort getCentralDirectoryLength() {
    int length = 1;
    if (isBit0_modifyTimePresent()) {
        length += 4;
    }
    return new ZipShort(length);
}