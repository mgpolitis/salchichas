module WordSet
    (WordSet, vacio, esVacio)
where
    import Char

    suma1 = (+1)
    char2int :: Char -> Int
    char2int = ((ord 'a') `subtract`) . (ord)



    data WordSet = GNode Bool [WordSet]

    subTreeForChar :: Char -> WordSet -> WordSet
    subTreeForChar c (GNode b ts) = ts !! (char2int c)


    vacio :: WordSet
    vacio = GNode False []

    esVacio :: WordSet -> Bool
    esVacio (GNode False []) = True
    esVacio _ = False

    foldWordSet :: (Bool->c->b) -> ([b] -> c) -> WordSet -> b
    foldWordSet g h (GNode b ts) = g b (h (map (foldWordSet g h) ts ))

--     pertenece :: String -> WordSet -> Bool
--     pertenece [] =  sonIguales (GNode True [])
    --pertenece (c : cs) = foldWordSet () ()

    agregarPalabra :: String -> WordSet -> WordSet
    agregarPalabra [] (GNode b ts) = GNode True ts
    agregarPalabra (c : cs)= foldWordSet (Bool->c->WordSet) ([WordSet] -> c)

    --borrarPalabra :: WordSet -> String -> WordSet

    --cantidadQueEmpiezanCon :: WordSet -> String -> Integer

--     sonIguales :: WordSet -> WordSet -> Bool
--     sonIguales vacio = esVacio
--     sonIguales (GNode b ts) = foldWordSet (\b2 hijos_son_iguales -> (b2 == b) && hijos_son_iguales) (and)

    -- wordSet2list :: WordSet -> [String]

