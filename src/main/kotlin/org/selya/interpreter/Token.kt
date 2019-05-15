package org.selya.interpreter

open class Token(val tokenType: TokenType, val row: Int, val column: Int) {
    enum class TokenType {
        NUMBER,
        OPEN_BRACE,
        CLOSE_BRACE,
        OPEN_SQUARE_BRACE,
        CLOSE_SQUARE_BRACE,
        OPEN_CURLY_BRACE,
        CLOSE_CURLY_BRACE,
        QUESTION_MARK,
        COLON_SIGN,
        EQUALS_SIGN,
        OPERATION,
        IDENTIFIER,
        COMMA,
        EOL
    }

    override fun toString(): String {
        return tokenType.toString()
    }
}

class OperationToken(val operation: Char, row: Int, column: Int) : Token(TokenType.OPERATION, row, column)
class NumberToken(val number: Int, row: Int, column: Int) : Token(TokenType.NUMBER, row, column)
class IdentifierToken(val name: String, row: Int, column: Int) : Token(TokenType.IDENTIFIER, row, column)