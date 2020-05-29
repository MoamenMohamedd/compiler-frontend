%{
  #include <cstdio>
  #include <iostream>
  #include <stdio.h>
  #include <stdlib.h>
  #include <string.h>
  using namespace std;

  // stuff from flex that bison needs to know about:
  extern int yylex();
  extern int yyparse();
  extern FILE *yyin;
  extern int line_num;
 
  void yyerror(const char *s);
%}

%union {
    float number;
    char* id;
}


%token <number> NUM
%token <id> ID
%token INT FLOAT
%token IF_T ELSE WHILE_T 
%token ASSIGN RELOP ADDOP MULOP


%%

METHOD_BODY:
    STATEMENT_LIST {
        cout << "Done with reading the file" << endl
    }
    ;

STATEMENT_LIST:
    STATEMENT
    | STATEMENT_LIST STATEMENT
    ;

STATEMENT:
    DECLARATION
    | IF
    | WHILE
    | ASSIGNMENT
    ;

DECLARATION:
    PRIMITIVE_TYPE ID ';'{
        cout << "declaration statement with id: " << $2 << endl
    }
    ;

PRIMITIVE_TYPE:
    INT 
    | FLOAT
    ;

IF:
    IF_T '(' EXPRESSION ')' '{' STATEMENT '}' ELSE '{' STATEMENT '}'{
        cout << "If else statement" << endl
    }
    ;

WHILE:
    WHILE_T '(' EXPRESSION ')' '{' STATEMENT '}'{
        cout << "While statement" << endl
    }
    ;

ASSIGNMENT:
    ID ASSIGN EXPRESSION ';'{
        cout << "Assignment statement of ID = " << $1 << endl
    }
    ;

EXPRESSION:
    SIMPLE_EXPRESSION
    | SIMPLE_EXPRESSION RELOP SIMPLE_EXPRESSION
    ;

SIMPLE_EXPRESSION:
    TERM 
    | SIGN TERM 
    | SIMPLE_EXPRESSION ADDOP TERM
    ;
TERM: 
    FACTOR 
    | TERM MULOP FACTOR
    ;
FACTOR:
    ID
    | NUM 
    | '(' EXPRESSION ')'
    ;

SIGN:
     '+'
     | '-'
     ;
%%


int main(int, char *argv[]) {
  // open a file handle to a particular file:
  FILE *myfile = fopen(argv[1], "r");
  // make sure it's valid:
  if (!myfile) {
    cout << "I can't open a.snazzle.file!" << endl;
    return -1;
  }
  // Set lex to read from it instead of defaulting to STDIN:
  yyin = myfile;

  // Parse through the input:
  yyparse();
}

void yyerror(const char *s) {
  cout << "EEK, parse error on line " << line_num << "!  Message: " << s << endl;
  // might as well halt now:
  exit(-1);
}