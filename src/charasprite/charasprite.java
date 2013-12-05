package charasprite;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.synapsehaven.spiffy.Spiffy;

public class charasprite
{
	public static void main(String[] args)
	{
		// TODO: Change this default value, later; it is Mike's.
		int port = 1337;
		if (args.length > 0)
			port = Integer.parseInt(args[0]);
		
		Spiffy spiff = new CharaSpiffy();
		
		spiff.listen(port);
	}
	
	public static class CharaSpiffy extends Spiffy.Web
	{
		public Object handleConnection(InputStream in, OutputStream out)
		{
			RequestHeader rq = (RequestHeader)super.handleConnection(in, out);
			if (!rq.success) return null;
			
			String content = this.createContent(rq);
			
			PrintWriter pout = new PrintWriter(out);
			pout.print(
				"HTTP/1.1 200 OK\n" +
				"Content-Type: text/html\n" +
				"Content-Length: "+content.length() +
				"\n\n"+content);
			pout.flush();
			
			return content;
		}
		
		private String createContent(RequestHeader rq)
		{
			String content = "WHAT!?";
			return content;
		}
	}
}
