module WordSet
    (WordSet, vacio, esVacio)
where
    import Char

    suma1 = (+1)
    char2int :: Char -> Int
    char2int = ((ord 'a') `subtract`) . (ord)

    int2char :: Int -> Char
    int2char = (chr) . (+ (ord 'a'))

    zipWithIndex :: [a] -> [(Int, a)]
    zipWithIndex = innerZWI 0
    
    innerZWI :: Int -> [a] -> [(Int, a)]
    innerZWI i [] = []
    innerZWI i (x : xs) = (i, x) : (innerZWI (i+1) xs)   

    data GTree a = GNode a [WordSet] deriving (Eq)
    type WordSet = GTree Bool

    subTreeForChar :: Char -> WordSet -> WordSet
    subTreeForChar c (GNode b ts) = ts !! (char2int c)

    zipWithChar :: [a] -> [(Char, a)]
    zipWithChar as = map (\(i,e) -> (int2char i, e)) (zipWithIndex as)

    vacio :: WordSet
    vacio = GNode False []

    soloLambda :: WordSet
    soloLambda = GNode True []    

    newBranch :: [WordSet]
    newBranch = replicate 26 vacio

    newFullBranch :: [WordSet]
    newFullBranch = replicate 26 soloLambda

    esVacio :: WordSet -> Bool
    esVacio (GNode False []) = True
    esVacio _ = False

    foldWordSet :: (Bool->c->b) -> ([b] -> c) -> WordSet -> b
    foldWordSet g h (GNode b ts) = g b (h (map (foldWordSet g h) ts ))

    tamanio :: WordSet -> Int
    tamanio = foldWordSet (step) (sum) where
                step b = if b then (1+) else (0+)
    

    pertenece :: String -> WordSet -> Bool
    pertenece [] =  sonIguales (GNode True [])
    pertenece (c : cs) = \ws -> pertenece cs (subTreeForChar c ws)   

    agregarPalabra :: String -> WordSet -> WordSet
    agregarPalabra [] (GNode b ts) = GNode True ts
    --agregarPalabra (c : cs)= foldWordSet (Bool->c->WordSet) ([WordSet] -> c)

    --borrarPalabra :: WordSet -> String -> WordSet

    --cantidadQueEmpiezanCon :: WordSet -> String -> Int

    sonIguales :: WordSet -> WordSet -> Bool
    sonIguales = (==)

    wordSet2list :: WordSet -> [String]
    wordSet2list (GNode b ts) = let zipped = (zipWithChar (map wordSet2list ts))
                                    init = if b then [""] else []
                                in init ++ (concat (map (\(c, l) -> map (c:) l) (zipped)))
-- ++ (map (\(c,l) -> [c]:l) zipped )


