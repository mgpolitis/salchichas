module WordSet
    (WordSet, vacio, esVacio)
where
    import Char
    import Maybe

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

    zipWithChar :: [a] -> [(Char, a)]
    zipWithChar as = map (\(i,e) -> (int2char i, e)) (zipWithIndex as)

    setElement :: [a] -> a -> Int -> [a]
    setElement [] x n = error "No se puede settear ese indice"
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

    newBranchWithChar :: Char -> [WordSet]
    newBranchWithChar c = setElement newBranch soloLambda (char2int c)

    replaceBranch :: WordSet -> Char -> WordSet -> WordSet
    replaceBranch (GNode b ts) c branch = (GNode b (setElement ts branch (char2int c)))

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
    pertenece (c : cs) = \ws -> case (subTreeForChar c ws) of
                                    Nothing -> False
                                    Just branch -> pertenece cs branch

    agregarPalabra :: String -> WordSet -> WordSet
    agregarPalabra [] (GNode b ts) = GNode True ts
    agregarPalabra (c : cs) ws@(GNode b ts) = case (subTreeForChar c ws) of
                                    Nothing -> replaceBranch (GNode b newBranch) c (agregarPalabra cs vacio)
                                    Just branch -> replaceBranch ws c (agregarPalabra cs branch)

    aPalabras :: [String] -> WordSet -> WordSet
    aPalabras [] ws = ws
    aPalabras (str : strs) ws = aPalabras strs (agregarPalabra str ws)

    borrarPalabra ::  String -> WordSet -> WordSet
    borrarPalabra [] (GNode b ts) = GNode False ts
--     borrarPalabra (c : cs) ws = case (subTreeForChar c ws) of
--                                 Nothing -> ws
--                                 Just branch -> borrarPalabra cs branch

    cantidadQueEmpiezanCon :: String -> WordSet -> Int
    cantidadQueEmpiezanCon [] ws        = tamanio ws
    cantidadQueEmpiezanCon (c : cs) ws  = case (subTreeForChar c ws) of
                                        Nothing -> 0
                                        Just branch -> cantidadQueEmpiezanCon cs branch

    sonIguales :: WordSet -> WordSet -> Bool
    sonIguales = (==)

    wordSet2list :: WordSet -> [String]
    wordSet2list (GNode b ts) = let zipped = (zipWithChar (map wordSet2list ts))
                                    init = if b then [""] else []
                                in init ++ (concat (map (\(c, l) -> map (c:) l) (zipped)))

    w2l :: WordSet -> [String]
    w2l = wordSet2list


