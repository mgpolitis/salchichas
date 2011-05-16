module WordSet
    (WordSet, vacio, esVacio)
where

    suma1 = (+1)

    data WordSet = GNode Bool [WordSet]

    vacio :: WordSet
    vacio = GNode False []

    esVacio :: WordSet -> Bool
    esVacio (GNode False []) = True
    esVacio _ = False

--pertenece :: WordSet -> String -> Bool

--agregarPalabra :: WordSet -> String -> WordSet

--borrarPalabra :: WordSet -> String -> WordSet

--cantidadQueEmpiezanCon :: WordSet -> String -> Integer

--sonIguales :: WordSet -> WordSet -> Bool



