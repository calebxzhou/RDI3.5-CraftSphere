package calebxzhou.rdi.consts

/**
 * Created  on 2023-02-25,9:22.
 */
enum class MessageType(content: String) {
    Info("ℹ  提示"),
    Warn("⚠  警告"),
    Error("\uD83D\uDEAB  错误"),
    Ask("❔  询问")
}
