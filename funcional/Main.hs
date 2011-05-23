module Main where
import WordSet

miWordSet = vacio

w2l vacio
-- []
w2l (agregarPalabra "" vacio)
-- [""]

resp1 = sonIguales (GNode True newFullBranch) (GNode True [GNode True [] | i<- [1..26]])
-- True
resp2 = w2l (GNode True newFullBranch)
-- ["","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"]
resp3 = cantidadQueEmpiezanCon "a" (GNode True newFullBranch)
-- 1
resp4 = cantidadQueEmpiezanCon "" (GNode True newFullBranch)
-- 27
resp5 = cantidadQueEmpiezanCon "p" (GNode True newFullBranch)
-- 1
resp6 = cantidadQueEmpiezanCon "pe" (GNode True newFullBranch)
-- 0
