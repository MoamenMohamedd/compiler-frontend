%{
  #include "compiler.tab.h"
  #include <stdio.h>
	#include <stdlib.h>
	#include <string.h>
  int line_num = 1;
%}

LETTER      [a-zA-Z]
DIGIT       [0-9]
DIGITS      DIGIT+
ID          {LETTER}({LETTER}|{DIGIT})*
INT         "-"?{DIGIT}+
FLOAT       "-"?{DIGIT}+\.{DIGIT}+([E]{DIGITS})?
RELOP       "=="|"!="|">"|">="|"<"|"<="
ADDOP       "+"|"-"
MULOP       "*"|"/"|"%"
BINOP       "&&"|"||"
BINARY      "true"|"false"

%%
"int"           {return (INT_WORD);}
"float"         {return (FLOAT_WORD);}
"boolean"        {return (BOOL_WORD);}
"="             {return (ASSIGN);}
"if"            {return (IF_WORD);}
"else"          {return (ELSE);}
"while"         {return (WHILE_WORD);}
[;,\(\)\{\}]    {return (yytext[0]); }
{BINARY}        {if(!strcmp(yytext,"true")){ yylval.bval = 1;} else { yylval.bval = 0;} return BOOL;}
{ID}            {yylval.id = strdup(yytext); return (ID);}
{INT}           {yylval.ival = atoi(yytext); return (INT);} 
{FLOAT}         {yylval.fval = atof(yytext); return (FLOAT);}
{RELOP}         {yylval.opval = strdup(yytext); return (RELOP);}
{ADDOP}         {yylval.opval = strdup(yytext); return (ADDOP);}
{MULOP}         {yylval.opval = strdup(yytext); return (MULOP);}
{BINOP}         {yylval.opval = strdup(yytext); return (BINOP);}
"\n"            { line_num += 1;}
[ \t]          ;
.               {perror(yytext);}
%%

int yywrap(void){
  return 1;
}

//{BINOP}         {yylval.opval = strdup(yytext); return (BINOP);}
