package charasprite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.synapsehaven.spiffy.Spiffy;

public class charasprite
{
	public static void main(String[] args)
	{
		Map<String,String> options = null;
		if (args.length > 0)
		{
			options = parseArguments(args);
		}
		
		CharaSpiffy cspiff = new CharaSpiffy(options);
		
		cspiff.listen();
	}
	
	// TODO: Decide whether arguments should be parsed statically, or
	//   if they ought be determined at the CharaSpiffy level.
	private static Map<String,String> parseArguments(String[] args)
	{
		Map<String,String> ret = new HashMap<String,String>();
		
		String s = null; 
		for (int i=0; i < args.length; ++i)
		{
			s = args[i];
			if (Pattern.matches("^-[^- \t]+", s))
			{
				// It's an option!
				s = s.substring(1);
				
				if (s.equalsIgnoreCase("rootdir"))
				{
					ret.put(s, args[++i]);
					continue;
				}
				else if (s.equalsIgnoreCase("port"))
				{
					ret.put(s, args[++i]);
					continue;
				}
			}
		}
		
		return ret;
	}
	
	
	public static class CharaSpiffy extends Spiffy.Web
	{
		public CharaSpiffy() {this(null);}
		public CharaSpiffy(Map<String,String> options)
		{
			// TODO: Maybe replace these strings with constant
			//   static values from somewhere.
			if (options != null)
			{
				if (options.containsKey("rootdir"))
					rootDirectory = options.get("rootdir");
				if (options.containsKey("port"))
					port = Integer.parseInt(options.get("port"));
			}
		}
		
		public final static int defaultPort = 10801;
		
		protected int port = defaultPort;
		protected String rootDirectory = "html";
		
		public void listen()
		{
			this.listen(port);
		}
		public void listen(int port)
		{
			System.out.println("Socket connecting to port: "+port);
			System.out.println("Root directory: "+rootDirectory);
			super.listen(port);
		}
		
		public Object handleConnection(InputStream in, OutputStream out)
		{
			RequestHeader rq = (RequestHeader)super.handleConnection(in, out);
			if (!rq.success) return null;
			
			// There is probably data if it is POST
			if (rq.method == RequestMethod.POST)
			{
				System.out.println("Well hey! Content-Type: "+rq.contentType);
				
				InputStream sin = null;
				OutputStream sout = null;
				try {
					Socket sock = this.serverSock.accept();
					sin = sock.getInputStream();
					sout = sock.getOutputStream();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				
				for (int i = 0; i < 100; ++i)
				{
					System.out.println("NEXT 1");
					byte[] these = new byte[1];
					try { sin.read(these); }
					catch (IOException e) { e.printStackTrace(); }
					System.out.println(new String(these));
				}
			}
			
			// Get the content data, given the request header.
			Map<String,Object> contentData = new HashMap<String,Object>();
			byte[] content = this.createContent(rq,contentData);
			
			HeaderType ht = HeaderType.TextHtml;
			if (contentData.containsKey("headerType"))
				ht = (HeaderType)contentData.get("headerType");
			else
			{
				System.err.println("ERROR: createContent did not resolve a HeaderType.");
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
		
		@SuppressWarnings("resource")
		protected byte[] createContent(RequestHeader rq, Map<String,Object> data)
		{
			if (rq.method == Spiffy.Web.RequestMethod.GET)
			{
				// Eh... for now, just serve a file.
				File f = new File(rootDirectory+rq.uri);
				
				if (f.exists())
				{
					if (data != null)
					{
						data.put("absolutePath", f.getAbsolutePath());
						data.put("fileExtension", f.getName().replaceFirst("^.*\\.", ""));
					}
				}
				
				// Create byte[] and fill with file data.  byte[] ret will return
				// at the close of this if-block.
				byte[] ret = new byte[(int)f.length()];
				try {
					FileInputStream fin = new FileInputStream(f);
					fin.read(ret);
				}
				catch(FileNotFoundException e)
				{ e.printStackTrace(); return null; }
				catch(IOException e)
				{ e.printStackTrace(); return null; }
				
				// Image file.
				if (Pattern.matches(".*\\.(bmp|gif|jpg|png|svg)$", f.getName()))
				{
					// Acquire header type metadata.
					if (data != null)
					{
						// Ordered by (personally) perceived likeliness.
						if (f.getName().endsWith(".png"))
							data.put("headerType", HeaderType.ImagePng);
						else if (f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg"))
							data.put("headerType", HeaderType.ImageJpg);
						else if (f.getName().endsWith(".gif"))
							data.put("headerType", HeaderType.ImageGif);
						else if (f.getName().endsWith(".bmp"))
							data.put("headerType", HeaderType.ImageBmp);
						else if (f.getName().endsWith(".svg"))
							data.put("headerType", HeaderType.ImageSvgXml);
					}
				}
				
				// Text-based file.
				else
				{
					if (data != null)
					{
						if (f.getName().endsWith(".js"))
							data.put("headerType", HeaderType.TextJavascript);
						else if (f.getName().endsWith(".htm") || f.getName().endsWith(".html"))
							data.put("headerType", HeaderType.TextHtml);
						else
							data.put("headerType", HeaderType.TextPlain);
					}
				}
				
				// Return byte[] for GET method case.
				return ret;
			}
			else if (rq.method == RequestMethod.POST)
			{
				System.err.println("contentType: "+rq.contentType);
				if (rq.contentType.indexOf("multipart/form-data") >= 0)
				{
					data.put("headerType", HeaderType.TextPlain);
				}
				
				return "Something, surely.".getBytes();
			}
			
			return null;
		}
		
		protected String headerStringFormat(HeaderType ht, Map<String,Object> param)
		{
			String contentType = null;
			
			switch(ht)
			{
				case TextPlain:
					contentType = "text/plain"; break;
				case TextHtml:
					contentType = "text/html"; break;
				case TextJavascript:
					contentType = "text/javascript"; break;
				case Image:
					contentType = "image/%s"; break;
				case ImageBmp:
					contentType = "image/bmp"; break;
				case ImageGif:
					contentType = "image/gif"; break;
				case ImageJpg:
					contentType = "image/jpg"; break;
				case ImagePng:
					contentType = "image/png"; break;
				case ImageSvgXml:
					contentType = "image/svg+xml"; break;
				default:
					return null;
			}
			
			String headerString =
				"HTTP/1.1 200 OK%n" +
				"Content-Type: "+contentType+"%n" +
				"Content-Length: %d" +
				"%n%n";
			return headerString;	
		}
		
		
		public static enum HeaderType
		{
			Image,
			ImageBmp,
			ImageGif,
			ImageJpg,
			ImagePng,
			ImageSvgXml,
			MultipartFormData,
			TextHtml,
			TextJavascript,
			TextPlain
		}
	}
}
