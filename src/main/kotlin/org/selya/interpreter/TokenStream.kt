package org.selya.interpreter

import java.lang.StringBuilder

class TokenStream(src: String) {
    companion object {
        fun tokenize(src: String): List<Token> {
            val tokens = ArrayList<Token>()
            val numberBuilder = StringBuilder()
            val identifierBuilder = StringBuilder()

            var row = 1
            var column = 1

            fun checkNumberAndIdentifier(symbol: Char? = null) {
                if (symbol in '0'..'9') {
                    numberBuilder.append(symbol)
                } else if (numberBuilder.isNotEmpty()) {
                    tokens.add(NumberToken(numberBuilder.toString().toInt(), row, column))
                    numberBuilder.setLength(0)
                }
                if (symbol in 'A'..'Z' || symbol in 'a'..'z' || symbol == '_') {
                    identifierBuilder.append(symbol)
                } else if (identifierBuilder.isNotEmpty()) {
                    tokens.add(IdentifierToken(identifierBuilder.toString(), row, column))
                    identifierBuilder.setLength(0)
                }
            }

            for (symbol in src) {
                checkNumberAndIdentifier(symbol)
                when (symbol) {
                    '(' -> tokens.add(Token(Token.TokenType.OPEN_BRACE, row, column))
                    ')' -> tokens.add(Token(Token.TokenType.CLOSE_BRACE, row, column))
                    '[' -> tokens.add(Token(Token.TokenType.OPEN_SQUARE_BRACE, row, column))
                    ']' -> tokens.add(Token(Token.TokenType.CLOSE_SQUARE_BRACE, row, column))
                    '{' -> tokens.add(Token(Token.TokenType.OPEN_CURLY_BRACE, row, column))
                    '}' -> tokens.add(Token(Token.TokenType.CLOSE_CURLY_BRACE, row, column))
                    '+', '-', '/', '*', '%', '<', '>' -> tokens.add(OperationToken(symbol, row, column))
                    '?' -> tokens.add(Token(Token.TokenType.QUESTION_MARK, row, column))
                    ':' -> tokens.add(Token(Token.TokenType.COLON_SIGN, row, column))
                    '=' -> tokens.add(Token(Token.TokenType.EQUALS_SIGN, row, column))
                    ',' -> tokens.add(Token(Token.TokenType.COMMA, row, column))
                    '\n' -> tokens.add(Token(Token.TokenType.EOL, row, column))
                }
                if (symbol == '\n') {
                    row++
                    column = 1
                } else {
                    column++
                }
            }
            checkNumberAndIdentifier()
            return tokens
        }
    }

    private val tokens: List<Token> = tokenize(src)
    private var nextTokenIndex = 0
    private var marker = 0

    fun mark() {
        marker = nextTokenIndex
    }

    fun reset() {
        nextTokenIndex = marker
    }

    fun rewind() {
        nextTokenIndex = 0
    }

    fun peekToken(): Token {
        if (nextTokenIndex >= tokens.size) throw InterpreterParseException("No next token")
        return tokens[nextTokenIndex]
    }

    fun nextToken(): Token {
        if (nextTokenIndex >= tokens.size) throw InterpreterParseException("No next token")
        return tokens[nextTokenIndex++]
    }

    fun assertNextToken(tokenType: Token.TokenType): Token {
        val token = nextToken()
        if (token.tokenType != tokenType) throw InterpreterParseException(
                "Expected " + tokenType.toString() + ", got " + token.tokenType.toString() + " " + token.row + ":" + token.column)
        return token
    }

    fun assertPeekToken(tokenType: Token.TokenType): Token {
        val token = peekToken()
        if (token.tokenType != tokenType) throw InterpreterParseException(
                "Expected " + tokenType.toString() + ", got " + token.tokenType.toString() + " " + token.row + ":" + token.column)
        return token
    }

    fun hasNextToken(): Boolean {
        return nextTokenIndex < tokens.size
    }
}

