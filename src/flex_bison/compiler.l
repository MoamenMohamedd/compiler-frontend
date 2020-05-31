%{
  #include "compiler.tab.h"
  int line_num = 1;
%}

LETTER      [a-zA-Z]
DIGIT       [0-9]
DIGITS      DIGIT+
ID          {LETTER}({LETTER}|{DIGIT})*
NUM         {DIGIT}+|{DIGIT}+\.{DIGIT}+([E]{DIGITS})?

%%
"int"             {return (INT);}
"float"           {return (FLOAT);}
"=="|"!="|">"|">="|"<"|"<="                 {return (RELOP);}
"="             {return (ASSIGN);}
"if"            {return (IF_T);}
"else"          {return (ELSE);}
"while"         {return (WHILE_T);}
[;,\(\)\{\}]    { return (yytext[0]); }
"+"|"-"         {return (ADDOP);}
"*"|"/"         {return (MULOP);}
{ID}            {yylval.id = strdup(yytext); return (ID);}
{NUM}           {yylval.number = atof(yytext); return (NUM);} 
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