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

`java -jar alv.jar [ <options> ] [ <mafia-log-directory> [ <parsed-log-directory> ] ]`

Running it without the `-p` option or its synonyms brings up a GUI, with all options and parameters ignored.  If you use the `-p` option or a synonym, then ALV will parse logs according to the options and parameters and quit.  ALV takes the following parameters:

* `-p` or `-parse` or `--parse`: Generate the parsed logs for the specified ascensions and quit.

The following format options are mutually exclusive, so specify only one, if any:

* `-text` or `--text`: Generate parsed logs in plaintext format.  This is the default.  
* `-xml` or `--xml`: Generate parsed logs in XML format.  
* `-html` or `--html`: Generate parsed logs in HTML format.  
* `-bbcode` or `--bbcode`: Generate parsed logs in BBcode format.  

The following options allow you to specify ascensions to parse:

* `-a <asc>` or `--ascension <asc>`: Begin parsing with ascension number `asc`.  Note that this number is one below the ascesnion number given by http://koldb.com.  
* `-c <n>` or `-count <n>` or `--count <n>`: Produce the specified number of parsed logs.  Defaults to 1, unless neither `-a`, `-d`, or their synonyms are specified, in which case all Mafia logs are parsed.  
* `-d <yyyymmdd>` or `--date <yyyymmdd>`: Begin parsing with the last ascension beginning on or before the specified date.  The date is specified as an 8-digit sequence `yyyymmdd` where `yyyy` is the year, `mm` is the month, and `dd` is the day.  For example, April 6, 2020, would be `20200406`.  
* `-n <name>` or `--name <name>`: Parse only logs belonging to player named `name`, handy if you have multis.  Defaults to the player that has the most ascensions in the Mafia logs.  

ALV takes the following optional arguments:

* `<mafia-log-directory>`: Specifies the directory from which to read the Mafia logs.  Defaults to the directory from which Mafia logs were read most recently.
* `<parsed-log-directory>`: Specifies the directory to which to write the parsed logs.  Defaults to the directory to which parsed logs were written most recently.

## Licensing and Contributors

TODO
