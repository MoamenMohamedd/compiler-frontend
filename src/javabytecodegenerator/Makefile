OBJS	= bison.o lex.o

CC	= g++
CFLAGS	= -g -Wall -ansi -pedantic -std=c++11

compiler:		$(OBJS)
		$(CC) $(CFLAGS) $(OBJS) -o compiler

lex.o:	lex.c tok.h
		$(CC) $(CFLAGS) -c lex.c -o lex.o -w

lex.c:		lex.l
		flex lex.l
		cp lex.yy.c lex.c

bison.o:	bison.c
		$(CC) $(CFLAGS) -c bison.c -o bison.o

bison.c:	parse.y lex.c
		bison --warnings=none -d -v parse.y
		cp parse.tab.c bison.c
		cp parse.tab.h tok.h
clean:
	rm -f *.o *~ lex.c lex.yy.c bison.c tok.h parse.tab.c parse.tab.h parse.output output.j compiler test.class