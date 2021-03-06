@ECHO OFF
:: ---------------------------------------------------------------------
:: Startup script for Gramlab docparsers command line client
:: ---------------------------------------------------------------------


::
:: Command line parameters :
::
:: --in or -i = Input file or directory (required)
:: --out or -o = Output directory (required)
::

::
:: Sample :
:: java -jar lib/gramlab-docparsers-%VERSION%.jar -i ./in -o ./out
::

java -jar lib/gramlab-docparsers-%VERSION%.jar -i %1 -o %2