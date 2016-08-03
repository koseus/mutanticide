import static org.junit.Assert.*;

import org.junit.Test;

public class FibonacciTest {

	@Test
	public void test() {
		String[] arg = new String[1];
		arg[0] = "17";
		assertEquals(2584, Fibonacci.main(arg));
	}

}
