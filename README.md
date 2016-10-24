# haplotype

This tool is an implementation of the [haplotype inference](https://en.wikipedia.org/wiki/Haplotype_estimation) method described in the article below.

[SAT in Bioinformatics: Making the Case with Haplotype Inference by Inês Lynce, João Marques-silva](http://link.springer.com/chapter/10.1007%2F11814948_16)

It consists in translating the haplotypes into a [SAT](https://en.wikipedia.org/wiki/Boolean_satisfiability_problem) CNF instance and then feeding it to a SAT solver. After it returns a set of valid values, these are translated back into the parent haplotypes.

It also contains the code for the SAT solvers [Glucose](http://www.labri.fr/perso/lsimon/glucose/) and [Minisat](http://minisat.se/) to perform the SAT solving part.

Historical note: this was developed as part of the credit requirements for my Master's in UFMG. I would like to thank Newton Vieira for his [class](http://homepages.dcc.ufmg.br/~nvieira/cursos/lac/a11s2/material.html) which was as challenging as it was rewarding.
