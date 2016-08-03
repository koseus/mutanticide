import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

public class DiningPhilTest {
	
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
	    System.setOut(null);
	    System.setErr(null);
	}
	
	@Test (timeout=5000)
	public void out() {
		DiningPhilosopher.main(null);
	    assertEquals("50", outContent.toString());
	}

//	@Test
//	public void err() {
//	    System.err.print("hello again");
//	    assertEquals("hello again", errContent.toString());
//	}
}
