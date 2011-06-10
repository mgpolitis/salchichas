module WordSet
    (WordSet, vacio, esVacio,
    pertenece, agregarPalabra, borrarPalabra, aPalabras, bPalabras,
    cantidadQueEmpiezanCon, tamanio,
    wordSet2list, w2l,
    sonIguales)
where
    import Char
    import Maybe

    -- ************
    --  utilidades
    -- ************


    suma1 = (+1)

    -- idea tomada de http://www.haskell.org/haskellwiki/If-then-else
    if' :: a -> a -> Bool -> a
    if' x _ True = x
    if' _ y False = y

    char2int :: Char -> Int
    char2int = ((ord 'a') `subtract`) . (ord)

    int2char :: Int -> Char
    int2char = (chr) . (+ (ord 'a'))

    zipWithIndex :: [a] -> [(Int, a)]
    zipWithIndex = innerZWI 0

    innerZWI :: Int -> [a] -> [(Int, a)]
    innerZWI i [] = []
    innerZWI i (x : xs) = (i, x) : (innerZWI (i+1) xs)

    zipWithChar :: [a] -> [(Char, a)]
    zipWithChar as = map (\(i,e) -> (int2char i, e)) (zipWithIndex as)

    setElement :: [a] -> a -> Int -> [a]
    setElement [] x n = error "El conjunto de palabras solo soporta letras minusculas"
    setElement (y:ys) x 0 = x:ys
    setElement (y:ys) x n = y:(setElement ys x (n-1))

    data GTree a = GNode a [WordSet] deriving (Eq)
    type WordSet = GTree Bool

    subTreeForChar :: Char -> WordSet -> Maybe WordSet
    subTreeForChar c (GNode b []) = Nothing
    subTreeForChar c (GNode b ts) = Just (ts !! (char2int c))

    vacio :: WordSet
    vacio = GNode False []

    soloLambda :: WordSet
    soloLambda = GNode True []

    newBranch :: [WordSet]
    newBranch = replicate 26 vacio

    replacePrune :: WordSet -> Char -> WordSet -> WordSet
    replacePrune ws@(GNode b ts) c branch = if (branch == vacio) && (tamanio ws) == 1
                                            then GNode b []
                                            else replaceBranch ws c branch

    replaceBranch :: WordSet -> Char -> WordSet -> WordSet
    replaceBranch (GNode b ts) c branch = GNode b (setElement ts branch (char2int c))

    newFullBranch :: [WordSet]
    newFullBranch = replicate 26 soloLambda

    turn :: Bool -> WordSet -> WordSet
    turn b (GNode o ts) = GNode b ts

    turnOff :: WordSet -> WordSet
    turnOff = turn False

    turnOn :: WordSet -> WordSet
    turnOn = turn True

    -- **********************
    --  funciones exportadas
    -- **********************

    -- dice si el conjunto es vacio o no
    esVacio :: WordSet -> Bool
    --esVacio (GNode False []) = True
    --esVacio _ = False
    esVacio = (==vacio)

    -- esquema recursivo para conjuntos de palabras
    foldWordSet :: (Bool->c->b) -> ([b] -> c) -> WordSet -> b
    foldWordSet g h (GNode b ts) = g b (h (map (foldWordSet g h) ts ))

    -- esquema para funciones que recorren strings y el arbol simultaneamente
    foldSWS :: (Char -> String -> WordSet -> t) -> (WordSet -> Char -> t -> t) -> (WordSet->t) -> String -> WordSet -> t
    foldSWS nf bf zf [] ws = zf ws
    foldSWS nf bf zf (c : cs) ws = case (subTreeForChar c ws) of
                            Nothing -> (nf c cs ws)
                            Just branch -> (bf ws c (foldSWS nf bf zf cs branch))

    -- devuelve la cantidad de palabras en el conjunto
    tamanio :: WordSet -> Int
    tamanio = foldWordSet (step) (sum) where
                step b = if b then (1+) else (0+)

    -- dice si la palabra pertenece al conjunto
    pertenece :: String -> WordSet -> Bool
    pertenece [] =  sonIguales (GNode True [])
    pertenece (c : cs) = \ws -> case (subTreeForChar c ws) of
                                    Nothing -> False
                                    Just branch -> pertenece cs branch

    -- agrega una palabra al conjunto
    agregarPalabra :: String -> WordSet -> WordSet
    --agregarPalabra [] (GNode b ts) = GNode True ts
    --agregarPalabra (c : cs) ws@(GNode b ts) = case (subTreeForChar c ws) of
    --                                Nothing -> replaceBranch (GNode b newBranch) c (agregarPalabra cs vacio)
    --                                Just branch -> replaceBranch ws c (agregarPalabra cs branch)
    agregarPalabra = foldSWS (\c cs (GNode b _) -> replaceBranch (GNode b newBranch) c (agregarPalabra cs vacio))
                            (replaceBranch) (turnOn)

    -- agrega todas las palabras de una lista a un conjunto
    aPalabras :: [String] -> WordSet -> WordSet
    --aPalabras [] ws = ws
    --aPalabras (str : strs) ws = aPalabras strs (agregarPalabra str ws)
    aPalabras = foldr (\str f -> f . (agregarPalabra str)) (id)


    -- borra una palabra del conjunto. si no esta, devuelve el conjunto original
    borrarPalabra ::  String -> WordSet -> WordSet
    --borrarPalabra [] (GNode b ts) = GNode False ts
    --borrarPalabra (c : cs) ws@(GNode b ts) = case (subTreeForChar c ws) of
    --                                Nothing -> ws
    --                                Just branch -> replaceBranch ws c (borrarPalabra cs branch)
    borrarPalabra = foldSWS (\_ _ ws -> ws) (replacePrune) (turnOff)

    -- borra todas las palabras de una lista a un conjunto
    bPalabras :: [String] -> WordSet -> WordSet
    --bPalabras [] ws = ws
    --bPalabras (str : strs) ws = bPalabras strs (borrarPalabra str ws)
    bPalabras = foldr (\str f -> f . (borrarPalabra str)) (id)


    -- devuelve la cantidad de palabras del conjunto que empiezan con el prefijo dado
    cantidadQueEmpiezanCon :: String -> WordSet -> Int
    --cantidadQueEmpiezanCon [] ws        = tamanio ws
    --cantidadQueEmpiezanCon (c : cs) ws  = case (subTreeForChar c ws) of
    --                                    Nothing -> 0
    --                                    Just branch -> cantidadQueEmpiezanCon cs branch
    cantidadQueEmpiezanCon = foldSWS (\_ _ _-> 0) (\_ _ n -> n) (tamanio)

    -- dice si ambos conjuntos contienen las mismas palabras
    sonIguales :: WordSet -> WordSet -> Bool
    sonIguales = (==)

    -- convierte el conjunto de palabras a una lista de palabras
    --wordSet2list :: WordSet -> [String]
    --wordSet2list (GNode b ts) = let zipped = (zipWithChar (map wordSet2list ts))
    --                                init = if b then [""] else []
    --                            in init ++ (concat (map (\(c, l) -> map (c:) l) (zipped)))
    wordSet2list = foldWordSet ((++).(if' [""] []))
                                (concat . (map (\(c, l) -> map (c:) l)) . zipWithChar)

    -- igual que wordSet2list, pero mas comodo
    w2l :: WordSet -> [String]
    w2l = wordSet2list


