rm paper.aux
xelatex paper.tex && biber paper && xelatex paper.tex && xelatex paper.tex
