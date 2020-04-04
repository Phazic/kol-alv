# kol-alv

This is a fork of Phazic's Ascension Log Visualizer, a utility for parsing KoLmafia ascension logs into standard, more readable formats.  My intent is to bring this tool back to the present.

## Building this project

Go to a Git directory and run 

`git clone https://github.com/m-e-meyer/kol-alv`

This will create a directory named kol-alv.  Go into that directory and, assuming you have ant, just run

`ant`

This will create alv.jar.

## Running ALV

You can run alv.jar like any other jar, with

`java -jar alv.jar`

Running it with no arguments brings up a GUI.  If you use the -p or -parse arguments, then ALV will parse logs and quit.  ALV takes the following parameters:

`-p` or `-parse`

`-xml` or `-html` or `-bbcode`

`-c` or `-count` \<number\>

## Licensing and Contributors

TODO
