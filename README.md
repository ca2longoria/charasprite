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
3. Open a browser, and navigate to the address _127.0.0.1:1337/index.template.html_.
  * Port 1337 is a temporary default.  The first argument of the java execution will be read as a port number, which will be used instead, if provided.
4. If you see a few silly pictures, it is working fine.
  * Note that you will have to stop the server manually from Eclipse.  There may presently be an issue where the contents of the html directory will block delete permissions while the socket is open.
