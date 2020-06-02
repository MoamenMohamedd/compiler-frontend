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
    float fval;
    int ival;
    char* id;
}


%token <ival> INT
%token <fval> FLOAT
%token <id> ID
%token INT_WORD FLOAT_WORD
%token IF_WORD ELSE WHILE_WORD 
%token ASSIGN RELOP ADDOP MULOP

%left ADDOP
%left MULOP

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
        cout << "id: " << $2 << endl ;
        cout << "End of declaration statement" << endl
    }
    ;

PRIMITIVE_TYPE:
    INT_WORD {
        cout << "Consumed Int " << endl ;
    }
    | FLOAT_WORD{
        cout  << "Consumed Float " << endl ;
    }
    ;

IF:
    {cout << "Consume IF" << endl ;}
    IF_WORD '(' EXPRESSION ')' {cout << "End of the if expression " << endl;} '{' STATEMENT '}' { cout << "Consume Else " << endl; } ELSE '{' STATEMENT '}'{
        cout << "End of If else statement" << endl
    }
    ;

WHILE:
    WHILE_WORD '(' EXPRESSION ')' '{' STATEMENT '}'{
        cout << "End of While statement" << endl
    }
    ;

ASSIGNMENT:
    ID ASSIGN EXPRESSION ';'{
        cout << " Assignment statement of ID = " << $1 << endl ;
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
    ID{
        cout << "ID = " << $1 << endl;
        free($1);
    }
    | INT
    | FLOAT
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