package charasprite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

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
		
		protected String createContent(RequestHeader rq)
		{
			String content = null;
			StringBuilder sb = new StringBuilder();
			
			if (rq.method == Spiffy.Web.RequestMethod.GET)
			{
				// Eh... for now, just serve a file.
				File f = new File("./html"+rq.uri);
				
				Scanner sc = null;
				try {
					sc = new Scanner(new FileInputStream(f));
					while (sc.hasNextLine())
					{
						String line = sc.nextLine();
						sb.append(line);
					}
					content = sb.toString();
				} catch (FileNotFoundException e)
				{
					// Serve... some kind of failure file.
					content = "Nothing?";
					e.printStackTrace();
				}
			}
			
			return content;
		}
	}
}
