public void applyAlias() {
    for (AliasedTypeNode aliasUsage : aliasUsages) {
        aliasUsage.applyAlias();
    }
}