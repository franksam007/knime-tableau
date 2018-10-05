
This plug-in is currently EMPTY as the Tableau Windows binaries suffer different problems. This is a conclusion we
draw after a call with folks at Tableau:


1) TAB_SDK_LOGDIR and TAB_SDK_TMPDIR need to be set on Windows systems, otherwise it fails with:
> com.tableausoftware.TableauException: Unknown error
>     at com.tableausoftware.extract.ExtractAPI.initialize(Unknown Source)
>     at org.knime.ext.tableau.tdewrite.TableauDENodeModel.execute(TableauDENodeModel.java:108)
(ideally you would not just fix this issue but add som API to set those variables programmatically so that we do not polute the user's home dir.)

2) Setting the PATH variable can be avoided by invoking
   NativeLibrary.addSearchPath("TableauExtract", folderPath.toString());
However, under Windows (only) it's not resolving the tdeserver64.exe, which is located in the same folder. Lee suggested to add ../bin to the search path.
> com.tableausoftware.TableauException: tdeserver64.exe is not on the environment path
>     at com.tableausoftware.extract.Extract.<init>(Unknown Source)
>     at org.knime.ext.tableau.TableauTableWriter.<init>(TableauTableWriter.java:97)

3) the ExtractAPI relies on the "Microsoft Visual Basic 2013 Redistributables", which Lee concluded might not be necessary. Ideally we would not require the user to install extra pieces.


These will be worked on in future releases of the Tableau SDK. 

User will need to set up the Tableau SDK on their machines manually. See the node description of the various Tableau
KNIME nodes.  