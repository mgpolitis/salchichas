
nextDiv :: Int -> Int -> Int
nextDiv x y = 2

sumDivs :: Int -> Int
sumDivs 1 = 1


power :: Int -> Int -> Int
power 0 b = 1
power e b = b * (power (e-1) b)


dividesTo :: Int -> Int -> Bool
dividesTo 1 b = True
dividesTo a b = False -- ??

prime :: Int -> Bool
prime 2 = True
prime n = not (any (\d -> mod n d == 0) [2..n-1])
