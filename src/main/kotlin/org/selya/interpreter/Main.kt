package org.selya.interpreter

fun main(args: Array<String>) {
    var src = ""
    while (true) {
        val line = readLine()
        if (line == null || line.isEmpty()) break
        src += line + "\n"
    }
    val parser = Parser(TokenStream(src))
    try {
        print(parser.compute())
    } catch (e: InterpreterRuntimeException) {
        // Do nothing (for testing purposes)
    } catch (e: InterpreterParseException) {
        // Same
    }
}