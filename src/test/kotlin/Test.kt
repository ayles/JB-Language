import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.selya.interpreter.Parser
import org.selya.interpreter.TokenStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class Test {

    @Test
    fun `Test calculating`() {
        assertEquals(Parser(TokenStream("(2+2)")).compute(), 4)
        assertEquals(Parser(TokenStream("(2+((3*4)/5))")).compute(), 4)
    }

    @Test
    fun `Test conditions`() {
        assertEquals(Parser(TokenStream("[((10+20)>(20+10))]?{1}:{0}")).compute(), 0)
    }

    @Test
    fun `Test functions`() {
        assertEquals(Parser(TokenStream(
                "g(x)={(f(x)+f((x/2)))}\n" +
                "f(x)={[(x>1)]?{(f((x-1))+f((x-2)))}:{x}}\n" +
                "g(10)"
        )).compute(), 60)
    }

    @Test
    fun `Test exceptions`() {
        val o = ByteArrayOutputStream()
        System.setOut(PrintStream(o))
        try { Parser(TokenStream("1 + 2 + 3 + 4 + 5")).compute() } catch (e: Exception) {}
        assert(o.toString().contains("SYNTAX ERROR"))
        o.reset()
        try { Parser(TokenStream(
                "f(x)={y}\n" +
                "f(10)"
        )).compute() } catch (e: Exception) {}
        assert(o.toString().contains("PARAMETER NOT FOUND y:1"))
        o.reset()
        try { Parser(TokenStream(
                "g(x)={f(x)}\n" +
                "g(10)"
        )).compute() } catch (e: Exception) {}
        assert(o.toString().contains("FUNCTION NOT FOUND f:1"))
        o.reset()
        try { Parser(TokenStream(
                "g(x)={(x+1)}\n" +
                "g(10,20)"
        )).compute() } catch (e: Exception) {}
        assert(o.toString().contains("ARGUMENT NUMBER MISMATCH g:2"))
        o.reset()
        try { Parser(TokenStream(
                "g(a,b)={(a/b)}\n" +
                "g(10,0)"
        )).compute() } catch (e: Exception) {}
        assert(o.toString().contains("RUNTIME ERROR (a/b):1"))
        o.reset()
    }
}