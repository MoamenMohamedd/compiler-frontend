%{
  #include "compiler.tab.h"
  int line_num = 0;
%}

LETTER      [a-zA-Z]
DIGIT       [0-9]
DIGITS      DIGIT+
ID          {LETTER}({LETTER}|{DIGIT})*
INT         {DIGIT}+
FLOAT       {DIGIT}+\.{DIGIT}+([E]{DIGITS})?

%%
"int"             {return (INT_WORD);}
"float"           {return (FLOAT_WORD);}
"=="|"!="|">"|">="|"<"|"<="                 {return (RELOP);}
"="             {return (ASSIGN);}
"if"            {return (IF_WORD);}
"else"          {return (ELSE);}
"while"         {return (WHILE_WORD);}
[;,\(\)\{\}]    { return (yytext[0]); }
"+"|"-"         {return (ADDOP);}
"*"|"/"         {return (MULOP);}
{ID}            {yylval.id = strdup(yytext); return (ID);}
{INT}           {yylval.ival = atoi(yytext); return (INT);} 
{FLOAT}         {yylval.fval = atof(yytext); return (FLOAT);}
"\n"            { line_num += 1;}
[ \t]          ;
.               {perror(yytext);}
%%

int yywrap(void){
  return 1;
}

// letter = a-z | A-Z
// digit = 0 - 9
// digits = digit+
// {boolean int float}
// num: digit+ | digit+ . digits ( \L | E digits)
// relop: \=\= | !\= | > | >\= | < | <\=
// assign: \=
// { if else while }
// [; , \( \) { }]
// addop: \+ | \-
// mulop: \* | /
// id: letter (letter|digit)*