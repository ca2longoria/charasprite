charasprite
===========

### Summary

A local browser application powered by tiff-java, the multipage TIFF writer, and spiffy, the super-simple java socket application server.

**charasprite** is intended to be used alongside other programs, like those of the RPG Maker family.

For now, it is but an amalglation of prototype jar files, and a bit of test html+media.

___
### Testing Instructions

1. Import into Eclipse as a Java Project.
2. Run charasprite.java as a Java Application.
3. Open a browser, and navigate to the address _127.0.0.1:10801/index.template.html_.
  * Port 10801 is the default.  The option "-port [port]" of the java execution will specify a port number, which will be used instead, if provided.
4. If you see a few silly pictures, it is working fine.
  * Note that you will have to stop the server manually from Eclipse.  There may presently be an issue where the contents of the html directory will block delete permissions while the socket is open.

**Alternatively...**

1. In the command-line, or a terminal or shell, _cd_ into the charasprite repo.
2. Verify the presence of the executable jar file, **charasprite.jar**.
  * Note that **charasprite.jar** will be behind the build's source in commit/revision, unless of course you deem to export it, again.
3. Run the following command:
  * java -jar charasprite.jar -port [desired port number] -rootdir [server root directory]
    * -port defaults to _10801_, and -rootdir defaults to _html_.
4. Open a browser, and navigate to the address _127.0.0.1:10801/index.template.html_.
  * Visible silly images means _victory_!
