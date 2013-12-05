package charasprite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
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
			
			Map<String,Object> contentData = new HashMap<String,Object>();
			byte[] content = this.createContent(rq,contentData);
			
			// TODO: Where should header-type be determined?  During content
			//   creation, or here in the handleConnection method? Both make
			//   reference to RequestHeader attributes...
			HeaderType ht = HeaderType.TextHtml;
			
			// WARNING: The URI may *not* end in the filename.  This need be
			//   resolved somewhere that makes sense.
			if (contentData.containsKey("headerType"))
				ht = (HeaderType)contentData.get("headerType");
			else
			{
				System.err.println("ERROR: createContent did not determine a HeaderType.");
				return null;
			}
			
			// First, the header.
			PrintWriter pout = new PrintWriter(out);
			pout.print(
				String.format(
					headerStringFormat(ht,null),
					content.length));
			pout.flush();
			
			// Now for the content.
			try { out.write(content); }
			catch (IOException e) {}
			
			return content;
		}
		
		protected byte[] createContent(RequestHeader rq, Map<String,Object> data)
		{
			if (rq.method == Spiffy.Web.RequestMethod.GET)
			{
				// Eh... for now, just serve a file.
				File f = new File("./html"+rq.uri);
				
				if (f.exists())
				{
					if (data != null)
					{
						data.put("absolutePath", f.getAbsolutePath());
						data.put("fileExtension", f.getName().replaceFirst("^.*\\.", ""));
					}
				}
				
				if (f.getName().endsWith(".png"))
				{
					byte[] ret = new byte[(int)f.length()];
					try {
						FileInputStream fin = new FileInputStream(f);
						fin.read(ret);
					}
					catch(FileNotFoundException e)
					{ e.printStackTrace(); return null; }
					catch(IOException e)
					{ e.printStackTrace(); return null; }
					
					if (data != null)
					{
						data.put("headerType", HeaderType.ImagePng);
					}
					
					return ret;
				}
				else
				{
					String content = null;
					StringBuilder sb = new StringBuilder();
				
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
					
					if (data != null)
					{
						// TODO: Distinguish between text file types (plain, html, javascript, ...)
						data.put("headerType", HeaderType.TextHtml);
					}
					
					return content.getBytes();
				}
			}
			
			return null;
		}
		
		protected String headerStringFormat(HeaderType ht, Map<String,Object> param)
		{
			switch(ht)
			{
				case TextPlain:
					return
						"HTTP/1.1 200 OK%n" +
						"Content-Type: text/plain%n" +
						"Content-Length: %d" +
						"%n%n";
				case TextHtml:
					return
						"HTTP/1.1 200 OK%n" +
						"Content-Type: text/html%n" +
						"Content-Length: %d" +
						"%n%n";
				case Image:
					return
						"HTTP/1.1 200 OK%n" +
						"Content-Type: image/%s%n" +
						"Content-Length: %d" +
						"%n%n";
				case ImagePng:
					return
						"HTTP/1.1 200 OK%n" +
						"Content-Type: image/png%n" +
						"Content-Length: %d" +
						"%n%n";
				default:
					return null;
			}
		}
		
		
		public static enum HeaderType
		{
			TextPlain,
			TextHtml,
			Image,
			ImagePng
		}
	}
}
