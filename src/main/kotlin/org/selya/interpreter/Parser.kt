package org.selya.interpreter

class InterpreterParseException(message: String) : Exception("SYNTAX ERROR") {
    constructor(message: String, token: Token) : this(message + " at ${token.row}:${token.column}")
}

class Parser(val tokenStream: TokenStream) {
    companion object {
        private fun parseBinaryExpression(tokenStream: TokenStream): BinaryExpressionNode {
            tokenStream.assertNextToken(Token.TokenType.OPEN_BRACE)
            val leftExpression = parse(tokenStream)
            val operation = tokenStream.assertNextToken(Token.TokenType.OPERATION) as OperationToken
            val rightExpression = parse(tokenStream)
            tokenStream.assertNextToken(Token.TokenType.CLOSE_BRACE)
            return BinaryExpressionNode(operation.operation, leftExpression, rightExpression, leftExpression.row, leftExpression.column)
        }

        private fun parseConditionalExpression(tokenStream: TokenStream): ConditionalExpressionNode {
            tokenStream.assertNextToken(Token.TokenType.OPEN_SQUARE_BRACE)
            val condition = parse(tokenStream)
            tokenStream.assertNextToken(Token.TokenType.CLOSE_SQUARE_BRACE)
            tokenStream.assertNextToken(Token.TokenType.QUESTION_MARK)
            tokenStream.assertNextToken(Token.TokenType.OPEN_CURLY_BRACE)
            val ifExpression = parse(tokenStream)
            tokenStream.assertNextToken(Token.TokenType.CLOSE_CURLY_BRACE)
            tokenStream.assertNextToken(Token.TokenType.COLON_SIGN)
            tokenStream.assertNextToken(Token.TokenType.OPEN_CURLY_BRACE)
            val elseExpression = parse(tokenStream)
            tokenStream.assertNextToken(Token.TokenType.CLOSE_CURLY_BRACE)
            return ConditionalExpressionNode(condition, ifExpression, elseExpression, condition.row, condition.column)
        }

        private fun parseFunction(tokenStream: TokenStream): Func {
            val name = (tokenStream.nextToken() as IdentifierToken).name
            tokenStream.assertNextToken(Token.TokenType.OPEN_BRACE)
            val argumentsList = ArrayList<String>()
            while (tokenStream.peekToken().tokenType != Token.TokenType.CLOSE_BRACE) {
                val identifier = tokenStream.nextToken() as IdentifierToken
                if (identifier.tokenType != Token.TokenType.IDENTIFIER)
                    throw InterpreterParseException("Error parsing arguments", identifier)
                argumentsList.add(identifier.name)
                if (tokenStream.peekToken().tokenType == Token.TokenType.COMMA)
                    tokenStream.nextToken()
            }
            tokenStream.assertNextToken(Token.TokenType.CLOSE_BRACE)
            tokenStream.assertNextToken(Token.TokenType.EQUALS_SIGN)
            tokenStream.assertNextToken(Token.TokenType.OPEN_CURLY_BRACE)
            val bodyExpression = parse(tokenStream)
            tokenStream.assertNextToken(Token.TokenType.CLOSE_CURLY_BRACE)
            tokenStream.assertNextToken(Token.TokenType.EOL)
            return Func(name, argumentsList, bodyExpression)
        }

        private fun parseFunctionDefinitionList(tokenStream: TokenStream): Map<String, Func> {
            val functionsTable = HashMap<String, Func>()

            while (true) {
                tokenStream.mark()
                if (tokenStream.nextToken().tokenType != Token.TokenType.IDENTIFIER ||
                        tokenStream.nextToken().tokenType != Token.TokenType.OPEN_BRACE)
                    break

                var bracesCount = 1
                while (bracesCount > 0) {
                    if (tokenStream.nextToken().tokenType == Token.TokenType.OPEN_BRACE)
                        bracesCount++
                    if (tokenStream.nextToken().tokenType == Token.TokenType.CLOSE_BRACE)
                        bracesCount--
                }

                if (!tokenStream.hasNextToken() || tokenStream.nextToken().tokenType != Token.TokenType.EQUALS_SIGN)
                    break

                tokenStream.reset()

                val f = parseFunction(tokenStream)
                functionsTable[f.name] = f
            }
            tokenStream.reset()
            return functionsTable
        }

        private fun parseIdentifier(tokenStream: TokenStream): ExpressionNode {
            val identifier = tokenStream.nextToken() as IdentifierToken
            if (tokenStream.peekToken().tokenType == Token.TokenType.OPEN_BRACE) {
                tokenStream.nextToken()
                val argumentsList = ArrayList<ExpressionNode>()
                while (tokenStream.peekToken().tokenType != Token.TokenType.CLOSE_BRACE) {
                    argumentsList.add(parse(tokenStream))
                    if (tokenStream.peekToken().tokenType == Token.TokenType.COMMA)
                        tokenStream.nextToken()
                }
                tokenStream.assertNextToken(Token.TokenType.CLOSE_BRACE)
                return CallExpressionNode(identifier.name, argumentsList, identifier.row, identifier.column)
            } else {
                return IdentifierExpressionNode(identifier.name, identifier.row, identifier.column)
            }
        }

        private fun parse(tokenStream: TokenStream): ExpressionNode {
            when (tokenStream.peekToken().tokenType) {
                Token.TokenType.OPEN_BRACE -> return parseBinaryExpression(tokenStream)
                Token.TokenType.NUMBER -> {
                    val n = tokenStream.nextToken() as NumberToken
                    return ConstantExpressionNode(n.number, n.row, n.column)
                }
                Token.TokenType.OPEN_SQUARE_BRACE -> return parseConditionalExpression(tokenStream)
                Token.TokenType.OPERATION -> {
                    val operation = tokenStream.nextToken() as OperationToken
                    if (operation.operation != '-')
                        throw InterpreterParseException("Got unsupported unary operation " + operation.operation)
                    if (tokenStream.peekToken().tokenType == Token.TokenType.NUMBER) {
                        val n = tokenStream.nextToken() as NumberToken
                        return NegateExpressionNode(ConstantExpressionNode(n.number, n.row, n.column), operation.row, operation.column)
                    }
                    tokenStream.assertPeekToken(Token.TokenType.IDENTIFIER)
                    return NegateExpressionNode(parseIdentifier(tokenStream), operation.row, operation.column)
                }
                Token.TokenType.IDENTIFIER -> {
                    return parseIdentifier(tokenStream)
                }
            }
            throw InterpreterParseException("Nothing parsed")
        }
    }

    private var functionTable: Map<String, Func>? = null
    private var expressionNode: ExpressionNode? = null

    private fun parse() {
        if (expressionNode == null) {
            functionTable = parseFunctionDefinitionList(tokenStream)
            expressionNode = parse(tokenStream)
            while (tokenStream.hasNextToken()) {
                if (tokenStream.nextToken().tokenType != Token.TokenType.EOL)
                    throw InterpreterParseException("SYNTAX ERROR")
            }
        }
    }

    fun compute(): Int {
        try {
            parse()
            return functionTable?.let { expressionNode?.compute(it) } ?: throw InterpreterRuntimeException("")
        } catch (e: InterpreterRuntimeException) {
            System.err.println(e.message)
            System.out.println(e.message) // IDK should it be stderr or stdout
            throw e
        } catch (e: InterpreterParseException) {
            System.err.println(e.message)
            System.out.println(e.message) // Same
            throw e
        }
    }
}