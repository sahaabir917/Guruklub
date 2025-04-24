
fun String.toElipsisTxt(): String {
    if (length > 20) {
        return "${substring(0, 20)}..."
    }
    return this
}