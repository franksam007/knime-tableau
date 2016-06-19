
This plug-in provides the Tableau SDK binaries, which are available from 
https://onlinehelp.tableau.com/current/api/sdk/en-us/help.htm#SDK/tableau_sdk.htm

The binaries come with their own license, for details see the Tableau SDK web page.

#####

Current version of the tableau binary is 9.3.1. Newer versions (tested 9.3.3) fail with:

java.lang.UnsatisfiedLinkError: Unable to load library 'TableauExtract': /usr/lib/x86_64-linux-gnu/libstdc++.so.6: versi
on `GLIBCXX_3.4.20' not found (required by /opt/knime_server_4.3/knime-full_3.2.0_2016-04-29_21-16-44/plugins/org.knime.
ext.tableau.bin.linux.x86_64_9.3.3.v201606181847/tableausdk-linux64-9300.0.0.0/lib64/tableausdk/libTableauExtract.so)
        at com.sun.jna.NativeLibrary.loadLibrary(NativeLibrary.java:194)
        at com.sun.jna.NativeLibrary.getInstance(NativeLibrary.java:283)
        at com.sun.jna.NativeLibrary.getInstance(NativeLibrary.java:244)
        at com.sun.jna.Native.register(Native.java:1065)
        at com.tableausoftware.extract.ExtractAPI.<clinit>(Unknown Source)
        at org.knime.ext.tableau.tdewrite.TableauDENodeModel.execute(TableauDENodeModel.java:108)
        at org.knime.core.node.NodeModel.execute(NodeModel.java:732)
