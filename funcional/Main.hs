module Main where
import WordSet

miWordSet = vacio

w2l vacio
-- []
w2l (agregarPalabra "" vacio)
-- [""]

sonIguales (GNode True newFullBranch) (GNode True [GNode True [] | i<- [1..26]])
-- True
w2l (GNode True newFullBranch)
-- ["","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"]
cantidadQueEmpiezanCon "a" (GNode True newFullBranch)
-- 1
cantidadQueEmpiezanCon "" (GNode True newFullBranch)
-- 27
cantidadQueEmpiezanCon "p" (GNode True newFullBranch)
-- 1
cantidadQueEmpiezanCon "pe" (GNode True newFullBranch)
-- 0

(agregarPalabra "pepe" (agregarPalabra "hola" vacio)) == (aPalabras ["hola", "pepe"] vacio)
-- True
w2l (aPalabras ["hola", "pepe", "a", "b", "c"] vacio)
-- ["a","b","c","hola","pepe"]
w2l (borrarPalabra "a" (aPalabras ["hola", "pepe", "a", "b", "c"] vacio))
-- ["b","c","hola","pepe"]
w2l (borrarPalabra "c" (borrarPalabra "b" (aPalabras ["hola", "pepe", "a", "b", "c"] vacio)))
-- ["a","hola","pepe"]
w2l (borrarPalabra "pepe" (aPalabras ["hola", "pepe", "a", "b", "c"] vacio))
-- ["a","b","c","hola"]
w2l (borrarPalabra "pep" (aPalabras ["hola", "pepe", "a", "b", "c"] vacio))
-- ["a","b","c","hola","pepe"]
w2l (borrarPalabra "" (aPalabras ["hola", "pepe", "a", "b", "c"] vacio))
-- ["a","b","c","hola","pepe"]
w2l (borrarPalabra "h" (aPalabras ["hola", "pepe", "a", "b", "c"] soloLambda))
-- ["","a","b","c","hola","pepe"]
w2l (borrarPalabra "" (aPalabras ["hola", "pepe", "a", "b", "c"] soloLambda))
-- ["a","b","c","hola","pepe"]
pertenece "" (borrarPalabra "" (aPalabras ["hola", "pepe", "a", "b", "c"] soloLambda))
-- False
pertenece "hola" (borrarPalabra "" (aPalabras ["hola", "pepe", "a", "b", "c"] soloLambda))
-- True
pertenece "hola" (borrarPalabra "hola" (aPalabras ["hola", "pepe", "a", "b", "c"] soloLambda))
-- False