from sympy import *

file = open("./tmp")
pr = file.read()
print to_dnf(pr, False)
file.close()
