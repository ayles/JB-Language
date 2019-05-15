package org.selya.interpreter

class InterpreterRuntimeException(override val message: String) : Exception(message)

abstract class ExpressionNode(val row: Int, val column: Int) {
    abstract fun compute(functionTable: Map<String, Func> = HashMap(), symbolTable: Map<String, Int> = HashMap()): Int
}

class NegateExpressionNode(val expressionNode: ExpressionNode, row: Int, column: Int) : ExpressionNode(row, column) {
    override fun compute(functionTable: Map<String, Func>, symbolTable: Map<String, Int>): Int {
        return -expressionNode.compute(functionTable, symbolTable)
    }

    override fun toString(): String {
        return "-$expressionNode"
    }
}

class ConstantExpressionNode(val number: Int, row: Int, column: Int) : ExpressionNode(row, column) {
    override fun compute(functionTable: Map<String, Func>, symbolTable: Map<String, Int>): Int = number

    override fun toString(): String {
        return number.toString()
    }
}

class BinaryExpressionNode(
        val operation: Char, val leftExpression: ExpressionNode, val rightExpression: ExpressionNode,
        row: Int, column: Int
) : ExpressionNode(row, column) {

    override fun compute(functionTable: Map<String, Func>, symbolTable: Map<String, Int>): Int {
        try {
            val left = leftExpression.compute(functionTable, symbolTable)
            val right = rightExpression.compute(functionTable, symbolTable)
            when (operation) {
                '+' -> return left + right
                '-' -> return left - right
                '*' -> return left * right
                '/' -> return left / right
                '%' -> return left % right
                '>' -> return if (left > right) 1 else 0
                '<' -> return if (left < right) 1 else 0
            }
        } catch (e: RuntimeException) {
            throw InterpreterRuntimeException("RUNTIME ERROR $this:$row")
        }
        return Int.MAX_VALUE
    }

    override fun toString(): String {
        return "($leftExpression$operation$rightExpression)"
    }
}

class ConditionalExpressionNode(
        val condition: ExpressionNode, val ifNode: ExpressionNode, val elseNode: ExpressionNode, row: Int, column: Int
) : ExpressionNode(row, column) {

    override fun compute(functionTable: Map<String, Func>, symbolTable: Map<String, Int>): Int {
        return if (condition.compute(functionTable, symbolTable) != 0) ifNode.compute(functionTable, symbolTable)
        else elseNode.compute(functionTable, symbolTable)
    }

    override fun toString(): String {
        return "[$condition]?{$ifNode}:{$elseNode}"
    }
}

class IdentifierExpressionNode(val name: String, row: Int, column: Int) : ExpressionNode(row, column) {
    override fun compute(functionTable: Map<String, Func>, symbolTable: Map<String, Int>): Int {
        return symbolTable[name] ?: throw InterpreterRuntimeException("PARAMETER NOT FOUND $name:$row")
    }

    override fun toString(): String {
        return name
    }
}

class ArgumentsMismatchException : Exception()

class Func(val name: String, val argumentNames: List<String>, val expressionNode: ExpressionNode) {
    fun compute(functionTable: Map<String, Func>, symbolTable: Map<String, Int>, arguments: List<ExpressionNode>): Int {
        if (arguments.size != argumentNames.size) {
            throw ArgumentsMismatchException()
        }
        val newSymbolTable = HashMap<String, Int>()
        for (i in argumentNames.indices) {
            newSymbolTable[argumentNames[i]] = arguments[i].compute(functionTable, symbolTable)
        }
        return expressionNode.compute(functionTable, newSymbolTable)
    }
}

class CallExpressionNode(val name: String, val arguments: List<ExpressionNode>, row: Int, column: Int) : ExpressionNode(row, column) {
    override fun compute(functionTable: Map<String, Func>, symbolTable: Map<String, Int>): Int {
        try {
            return functionTable[name]?.compute(functionTable, symbolTable, arguments)
                    ?: throw InterpreterRuntimeException("FUNCTION NOT FOUND $name:$row")
        } catch (e: ArgumentsMismatchException) {
            throw InterpreterRuntimeException("ARGUMENT NUMBER MISMATCH $name:$row")
        }
    }

    override fun toString(): String {
        var s = "$name("
        for (a in arguments) {
            s += a.toString() + ","
        }
        s += ")"
        return s
    }
}
