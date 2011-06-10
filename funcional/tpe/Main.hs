module Main where
import WordSet


miWordSet = vacio
test1 = esVacio miWordSet
-- test1 deberia ser True


-- creemos un conjunto de palabras para empezar
tres = (agregarPalabra "luis" (agregarPalabra "paco" (agregarPalabra "hugo" vacio)))

-- primero, verifiquemos que el tamaño sea el adecuado
test2 = tamanio tres == 3
-- test2 deberia ser True

-- y que las tres palabras agregadas estén en el conjunto
test3 = pertenece "hugo" tres
-- test3 deberia ser True

test4 = pertenece "paco" tres
-- test4 -> True

test5 = pertenece "luis" tres
-- test5 -> True

-- veamos ahora que no hay palabras no insertadas en el conjunto
test6 = any ((flip pertenece) tres) ["", "h", "p", "l", "hu", "pa", "lu", "hug", "pac", "lui", "hugos", "pacos", "luiss"]
-- test6 -> False

-- probemos ahora el metodo de agregado masivo, aPalabras, y veamos que es equivalente
tres' = aPalabras ["hugo", "paco", "luis"] vacio
test7 = sonIguales tres tres'
-- test7 -> True

-- empecemos a borrar
dos = borrarPalabra "hugo" tres
test8 = not (pertenece "hugo" dos) && pertenece "paco" dos && pertenece "luis" dos
test8' = tamanio dos == 2
-- test8 y test8'-> True

uno = borrarPalabra "paco" dos
test9 = not (pertenece "hugo" uno) && not (pertenece "paco" uno) && pertenece "luis" uno
test9' = tamanio uno == 1
-- test9 y test9' -> True

cero = borrarPalabra "luis" uno
test10 = not (pertenece "hugo" cero) && not (pertenece "paco" cero) && not (pertenece "luis" cero)
test10' = tamanio cero == 0
-- test10 y test10' -> True


-- veamos ahora que el metodo de borrado masivo, bPalabras es equivalente
cero' = bPalabras ["hugo", "paco", "luis"] tres
test11 = sonIguales cero cero'
-- test11 -> True

-- y veamos ahora que ambos son el conjunto vacio
test12 = esVacio cero && esVacio cero'
-- test12 -> True



-- veamos ahora el funcionamiento de la funcion cantidadQueEmpiezanCon
ws = aPalabras ["", "h", "hi", "hola", "ola", "holgado", "holos"] vacio
test13 = cantidadQueEmpiezanCon "" ws == 7
-- test13 -> True

test14 = cantidadQueEmpiezanCon "h" ws == 5
-- test13 -> True

test15 = cantidadQueEmpiezanCon "ho" ws == 3
-- test15 -> True

test16 = cantidadQueEmpiezanCon "hi" ws == 1
-- test16 -> True

test17 = cantidadQueEmpiezanCon "hol" ws == 3
-- test17 -> True

test18 = cantidadQueEmpiezanCon "ola" ws == 1
-- test18 -> True

test19 = cantidadQueEmpiezanCon "z" ws == 0
-- test19 -> True



-- finalmente, veamos el funcionamiento de la funcion w2l, que convierte WordSet en listas de palabras
test20 = ["hugo", "luis", "paco"] == w2l tres
--test20 -> True

test21 = [] == w2l vacio
--test21 -> True

test22 = [""] == w2l (agregarPalabra "" vacio)
-- test22 -> True

test23 = ["","h","hi","hola","holgado","holos","ola"] == w2l ws
-- test23 -> True



-- FIN











