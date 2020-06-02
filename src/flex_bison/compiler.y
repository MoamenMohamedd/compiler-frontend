%{
  #include <cstdio>
  #include <iostream>
  #include <stdio.h>
  #include <stdlib.h>
  #include <string.h>
  #include <map>
  using namespace std;

  // stuff from flex that bison needs to know about:
  extern int yylex();
  extern int yyparse();
  extern FILE *yyin;
  extern int line_num;
 
  void yyerror(const char *s);

  typedef enum {INT_T, FLOAT_T} type_enum;

  map<string, pair<int,type_enum>> symtable;
  int varaiblesNum = 1;

  void define_var(string name, int type);
  bool checkID(string name);

%}

%union {
    float fval;
    int ival;
    char* id;
    int type;
}

%start method_body

%token <ival> INT
%token <fval> FLOAT
%token <id> ID
%token INT_WORD FLOAT_WORD
%token IF_WORD ELSE WHILE_WORD 
%token ASSIGN RELOP ADDOP MULOP

%type<type> primitive_type

%left ADDOP
%left MULOP

%%

method_body:
    statement_list {
        cout << "Done with reading the file" << endl
    }
    ;

statement_list:
    statement
    | statement_list statement
    ;

statement:
    declaration
    | if
    | while
    | assignment
    ;

declaration:
    primitive_type ID ';'{
        if($1 == INT_T)
        {
            define_var($2,$1);
            cout << "type = INT" << endl;
        }else if($1 == FLOAT_T){
            define_var($2,$1);
            cout << "type = FLOAT" << endl;
        }
        cout << "id: " << $2 << endl ;
        cout << "End of declaration statement" << endl
    }
    ;

primitive_type:
    INT_WORD {
        $$ = INT_T;
    }
    | FLOAT_WORD {
        $$ = FLOAT_T;
    }
    ;

if:
    {cout << "Consume IF" << endl ;}
    IF_WORD '(' expression ')' {cout << "End of the if expression " << endl;} '{' statement '}' { cout << "Consume Else " << endl; } ELSE '{' statement '}'{
        cout << "End of If else statement" << endl
    }
    ;

while:
    WHILE_WORD '(' expression ')' '{' statement '}'{
        cout << "End of While statement" << endl
    }
    ;

assignment:
    ID ASSIGN expression ';'{
        cout << " Assignment statement of ID = " << $1 << endl ;
    }
    ;

expression:
    simple_expression
    | simple_expression RELOP simple_expression
    ;

simple_expression:
    term 
    | sign term 
    | simple_expression ADDOP term
    ;
term: 
    factor 
    | term MULOP factor
    ;
factor:
    ID{
        cout << "ID = " << $1 << endl;
        free($1);
    }
    | INT
    | FLOAT
    | '(' expression ')'
    ;

sign:
     '+'
     | '-'
     ;
%%


int main(int, char *argv[]) {
  // open a file handle to a particular file:
  FILE *myfile = fopen(argv[1], "r");
  // make sure it's valid:
  if (!myfile) {
    cout << "I can't open the input file!" << endl;
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

 void define_var(string name, int type)
{
    if(checkID(name)){
        string err = "Variable: " + name + " is declared before";
        yyerror(err.c_str());
    }
    else{
        if(type == INT_T){
            cout << "iconst_" + to_string(varaiblesNum) << endl;  
            cout << "istore_" + to_string(varaiblesNum) << endl;
        }
        else if(type == FLOAT_T){
        cout << "fconst_" + to_string(varaiblesNum) << endl; 
        cout << "fstore_" + to_string(varaiblesNum) << endl;
        }
        symtable[name] = make_pair(varaiblesNum++, (type_enum)type);
    }
}

bool checkID(string name){
    if(symtable.find(name) == symtable.end()){
        return false;
    }
    return true; 
}