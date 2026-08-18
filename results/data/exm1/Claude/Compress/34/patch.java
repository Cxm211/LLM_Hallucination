public ZipShort getCentralDirectoryLength() {
    return new ZipShort((isBit0_modifyTimePresent() ? 4 : 0) + 1);
}