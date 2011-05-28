f = foldr (:) []

concat' :: [[a]] -> [a]
concat' = foldr (++) []

filter' :: (a -> Bool) -> [a] -> [a]
filter' f = concat .(map (\x -> if f x then [x] else []))


takeWhile' :: (a->Bool) -> [a] -> [a]
takeWhile' f = foldr (\x -> if f x then (x:) else (const [])) []

dropWhile' :: (a->Bool) -> [a] -> [a]
dropWhile' f [] = []
dropWhile' f (x:xs) = if f x then (dropWhile' f xs) else (x:xs)
-- nose puede hacer sin recr, con foldr no alcanza

inits :: [a] -> [[a]]
inits = foldr (\x -> ([x]:).(map (x:))) []



