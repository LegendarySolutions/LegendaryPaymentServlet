package legacycode.fixtures;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

/**
 *
 * @author mcendrowicz
 */
public class DummyServletOutputStream extends ServletOutputStream {

	@Override
	public void write(int b) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void print(String s) throws IOException {
		System.out.println("DummyServletOutputStream prints " + s);
	}
	
	

}
