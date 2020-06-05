#!/bin/sh +xv

echo "Removing old files"
make -s clean
echo "Bulding java compiler"
make -s
echo "Running compiler on input file"
./compiler ../input-program-7.txt
echo "Generating class file"
java -jar jasmin-2.4/jasmin.jar output.j
echo "Executing java code"
java test