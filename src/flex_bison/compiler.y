%{
  #include <cstdio>
  #include <iostream>
  #include <stdio.h>
  #include <stdlib.h>
  #include <string.h>
  #include <map>
  #include <fstream>
  #include <cstring>
  #include <vector>
  #include <unistd.h>
  

  #include "bytecode_inst.h"


  using namespace std;

  // stuff from flex that bison needs to know about:
  extern int yylex();
  extern int yyparse();
  extern FILE *yyin;
  extern int line_num;
 
  void yyerror(const char *s);

  typedef enum {INT_T, FLOAT_T, BOOL_T, ERROR_T} type_enum;

  map<string, pair<int,type_enum>> symtable;
  vector<string> codeList;
  int varNumber = 1;
  int labelsCount = 0;
  string outfileName ;

  ofstream fout("output.j");
  	
  void generateHeader(void);	/* generate  header for class to be able to compile the code*/
  void generateFooter(void);	/* generate  footer for class to be able to compile the code*/

  void define_var(string name, int type);                       //define a new variable
  bool checkID(string name);                                    //check for the id in the symtable
  string genNewLabel();                                         //generate a new label
  string getLabel(int n);                                       //get the label
  void backpatch(vector<int> *lists, int ind);                  //
  void arithOperation(int from , int to, string op);            //to write the aithamtic operation bytecode
  void genByteCode(string x);                                   //write the bytecode in the codelist vector
  void writeToFile(void);                                         //write the code to out[ut file
  string getOperand(string op);                                 //get operand from header file
  vector<int> * merge(vector<int> *list1, vector<int> *list2);  //merge two lists

%}

%code requires {
	#include <vector>
	using namespace std;
}


%union {
    float fval;
    int ival;
    char* id;
    int type;
    char* opval;
    int bval;
	struct {
		vector<int> *trueList, *falseList;
	} bexpr_type;
	struct {
		vector<int> *nextList;
	} stmt_type;
}

%start method_body

%token <ival> INT
%token <fval> FLOAT
%token <id> ID
%token <bval> BOOL 
%token <opval> RELOP ADDOP MULOP BINOP 
%token INT_WORD FLOAT_WORD BOOL_WORD
%token IF_WORD ELSE WHILE_WORD 
%token ASSIGN 

%type<type> primitive_type
%type<stmt_type> statement
%type<stmt_type> statement_list
%type<stmt_type> if
%type<stmt_type> while
%type<type> expression arth_expression factor term
%type<bexpr_type> bool_expression

%type <ival> marker
%type <ival> goto


%left ADDOP
%left MULOP

%%

method_body:
    {	generateHeader();	}
	statement_list
	marker
	{
		backpatch($2.nextList,$3);
		generateFooter();
        writeToFile();
        cout << "Done reading file"<<endl;
	}
	;

statement_list:
    statement
    | statement_list marker statement
    {
  		backpatch($1.nextList,$2);
  		$$.nextList = $3.nextList;
  	}
    ;

marker:
{
	$$ = labelsCount;
	genByteCode(genNewLabel() + ":");
}
;

statement:
    declaration {vector<int> * v = new vector<int>(); $$.nextList =v;}
    | if {$$.nextList = $1.nextList;}
	| while 	{$$.nextList = $1.nextList;}
    | assignment {vector<int> * v = new vector<int>(); $$.nextList =v;}
    ;

declaration:
    primitive_type ID ';'{
        define_var($2,$1);
    }
    ;

primitive_type:
    INT_WORD {
        $$ = INT_T;
    }
    | FLOAT_WORD {
        $$ = FLOAT_T;
    }
    | BOOL_WORD {
        $$ = BOOL_T;
    }
    ;

goto:
{
	$$ = codeList.size();
	genByteCode("goto ");
}
;

if:
    IF_WORD '(' bool_expression ')' '{' marker statement_list goto '}' ELSE '{' marker statement_list '}'
    {
        backpatch($3.trueList,$6);
		backpatch($3.falseList,$12);
		$$.nextList = merge($7.nextList, $13.nextList);
		$$.nextList->push_back($8);
    }
    ;

while:
    marker WHILE_WORD '(' bool_expression ')' '{' marker statement_list '}'{
        genByteCode("goto " + getLabel($1));
		backpatch($8.nextList,$1);
		backpatch($4.trueList,$7);
		$$.nextList = $4.falseList;
    }
    ;

assignment:
    ID ASSIGN expression ';'{
        string str($1);
		if(checkID(str))
		{
			if($3 == symtable[str].second)
			{
				if($3 == INT_T || $3 == BOOL_T)
				{
					genByteCode("istore " + to_string(symtable[str].first));
				}else if ($3 == FLOAT_T)
				{
					genByteCode("fstore " + to_string(symtable[str].first));
				}
			}
			else
			{
				yyerror("This type is not supported");
			}
		}else{
			string err = "ID: "+str+" isn't declared in this scope";
			yyerror(err.c_str());
		};
    }
;

expression:
    arth_expression { $$ = $1; }
    | bool_expression { $$ = BOOL_T;}
    ;

arth_expression:
    arth_expression ADDOP term { arithOperation($1, $3, string($2)); }
    | term { $$ = $1; }
    ;

term:
    term MULOP factor { arithOperation($1, $3, string($2)); }
    | factor { $$ = $1; }
    ;

factor:
    ID
    {
      string str($1);
      if(checkID(str))
      {
        $$ = symtable[str].second;
        if($$ == INT_T || $$ == BOOL_T)
        {
          genByteCode("iload " + to_string(symtable[str].first));
        }
        else if ($$ == FLOAT_T)
        {
          genByteCode("fload " + to_string(symtable[str].first));
        }
      }
      else
      {
        string err = "Identifier: " + str + " has not been declared";
        yyerror(err.c_str());
        $$ = ERROR_T;
      }
    }
    | INT { $$ = INT_T; genByteCode("ldc " + to_string($1));}
    | FLOAT {$$ = FLOAT_T; genByteCode("ldc " + to_string($1));}
    | '(' arth_expression ')'
    ;

bool_expression:
    BOOL
	{
		if($1)
		{
			/* bool is 'true' */
			$$.trueList = new vector<int> ();
			$$.trueList->push_back(codeList.size());
			$$.falseList = new vector<int>();
			genByteCode("goto ");
		}else
		{
			$$.trueList = new vector<int> ();
			$$.falseList= new vector<int>();
			$$.falseList->push_back(codeList.size());
			genByteCode("goto ");
		}
	}
    | bool_expression BINOP marker bool_expression{
        if(!strcmp($2, "&&"))
		{
			backpatch($1.trueList, $3);
			$$.trueList = $4.trueList;
			$$.falseList = merge($1.falseList,$4.falseList);
		}
		else if (!strcmp($2,"||"))
		{
			backpatch($1.falseList,$3);
			$$.trueList = merge($1.trueList, $4.trueList);
			$$.falseList = $4.falseList;
		}
    }
    | expression RELOP expression{
        string op ($2);
		$$.trueList = new vector<int>();
		$$.trueList ->push_back (codeList.size());
		$$.falseList = new vector<int>();
		$$.falseList->push_back(codeList.size()+1);
		genByteCode(getOperand(op)+ " ");
		genByteCode("goto ");
    }
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
  outfileName = argv[1];

  // Parse through the input:
  yyparse();
}

void yyerror(const char *s) {
  cout << "EEK, parse error on line " << line_num << "!  Message: " << s << endl;
  // might as well halt now:
  exit(-1);
}

void generateHeader()
{
	genByteCode(".source " + outfileName);
	genByteCode(".class public test\n.super java/lang/Object\n"); //code for defining class
	genByteCode(".method public <init>()V");
	genByteCode("aload_0");
	genByteCode("invokenonvirtual java/lang/Object/<init>()V");
	genByteCode("return");
	genByteCode(".end method\n");
	genByteCode(".method public static main([Ljava/lang/String;)V");
	genByteCode(".limit locals 100\n.limit stack 100");

	/* generate temporal vars for syso*/
	define_var("1syso_int_var",INT_T);
	define_var("1syso_float_var",FLOAT_T);

	/*generate line*/
	genByteCode(".line 1");
}

void generateFooter()
{
	genByteCode("return");
	genByteCode(".end method");
}

 void define_var(string name, int type)
{
    if(checkID(name)){
        string err = "Variable: " + name + " is declared before";
        yyerror(err.c_str());
    }
    else{
        if(type == INT_T){
            genByteCode("iconst_0\nistore " + to_string(varNumber));
        }
        else if(type == FLOAT_T){
            genByteCode("fconst_0\nfstore " + to_string(varNumber));
        }
        symtable[name] = make_pair(varNumber++, (type_enum)type);
    }
}

bool checkID(string name){
    if(symtable.find(name) == symtable.end()){
        return false;
    }
    return true; 
}

string genNewLabel()
{
	return "L_"+to_string(labelsCount++);
}

string getLabel(int n)
{
	return "L_"+to_string(n);
}

void backpatch(vector<int> *lists, int ind)
{
	if(lists){
        for(int i =0 ; i < lists->size() ; i++)
	    {
		    codeList[(*lists)[i]] = codeList[(*lists)[i]] + getLabel(ind);
	    }
    }
	
}

void genByteCode(string x)
{
	codeList.push_back(x);
}

void writeToFile(void)
{
	for ( int i = 0 ; i < codeList.size() ; i++)
	{
		fout<<codeList[i]<<endl;
	}
}

void arithOperation(int from , int to, string op)
{
	if(from == to)
	{
		if(from == INT_T)
		{
			genByteCode("i" + getOperand(op));
		}else if (from == FLOAT_T)
		{
			genByteCode("f" + getOperand(op));
		}
	}
	else
	{
		yyerror("Cast not implemented yet");
	}
}

string getOperand(string op)
{
	if(inst_list.find(op) != inst_list.end())
	{
		return inst_list[op];
	}
	return "";
}

vector<int> * merge(vector<int> *list1, vector<int> *list2)
{
	if(list1 && list2){
		vector<int> *list = new vector<int> (*list1);
		list->insert(list->end(), list2->begin(),list2->end());
		return list;
	}else if(list1)
	{
		return list1;
	}else if (list2)
	{
		return list2;
	}else
	{
		return new vector<int>();
	}
}