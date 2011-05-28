


sum' :: [Int] -> Int
sum' = foldr (+) 0

and' :: [Bool] -> Bool
and' = foldr (&&) True

or' :: [Bool] -> Bool
or' = foldr (||) False

map' :: (a->b) -> [a] -> [b]
map' f = foldr ((:).f) []

filter' :: (a->Bool) -> [a] -> [a]
filter' f = foldr (\a -> if f a then ([a]++) else ([]++)) []

longerThan :: [[a]] -> Int -> [[a]]
longerThan = flip longerThan2

longerThan2 :: Int -> [[a]] -> [[a]]
longerThan2 n = filter ((>n).length)

pairs :: [Int] -> [Int]
pairs = filter even

squares :: [Int] -> [Int]
squares = map (\x -> x*x)

sp = squares.pairs
sp2 = pairs.squares

testEq :: (Eq b) => (a->b) -> (a->b) -> a -> Bool
testEq f g a = f a == g a

-- pairs.squares = squares.pairs
--      por principio de extensionalidad esto pasa sii
-- pairs.squares ys = squares.pairs ys
-- hagamos induccion en la estructura de ys
-- caso base ys = []
-- 
-- caso inductivo, asumiendo que ys = (x:xs) 
-- y que pairs.squares xs = squares.pairs xs
-- entonces:
-- pairs (squares xs) = squares (pairs xs)
--      veamos que pasa con el lhs     
-- pairs.squares ys = 
-- pairs (squares ys) = 
-- pairs (squares (x:xs)) =
-- pairs ( (x*x): (squares xs)) = 
-- if even (x*x) then (x*x):(squares xs) else (squares xs)
--      veamos que pasa con el rhs
-- squares.pairs ys =
-- squares (pairs ys) =
-- squares (pairs (x:xs)) = 
-- squares (if even (x) then (x):xs else xs)
-- y se sabe que even x = even (x*x)
-- si even x = True
--      lhs = (x*x):(squares xs) = squares (x:xs)
--      rhs = squares ((x):xs) = squares (x:xs)
-- iguales! analogamente para el caso even x = False


ifThenElse_Lam = \x -> x 
true_Lam = \x -> \y -> x 
false_Lam = \x -> \y -> y 
not_Lam = \x -> ifThenElse_Lam x false_Lam true_Lam
