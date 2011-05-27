data ColorPrimario = Amarillo | Rojo | Azul

data ColorSecundario = Verde | Naranja | Violeta

mezclar Amarillo Rojo = Naranja
mezclar Rojo Amarillo = Naranja
mezclar Amarillo Azul = Verde
mezclar Azul Amarillo = Verde
mezclar Rojo Azul = Violeta
mezclar Azul Rojo = Violeta


-- modulo, distanciaA, xcoord, ycoord y suma. 
data Point = Point2D Float Float | Point3D Float Float Float
modulo (Point2D x y) = sqrt (x^2 + y^2)
modulo (Point3D x y z) = sqrt (x^2 + y^2 + z^2)

distanciaA (Point2D x y) (Point2D z w) = sqrt ((x - z)^2 +(y - w)^2)

xCoord (Point2D x y) = x
yCoord (Point2D x y) = y

suma (Point2D x y) (Point2D z w) = Point2D (x+z) (y+w)


or' x y = x || y
first x y = x
pair f g x = (f x, g x)

suma' x y = x+y
uno x y  
    | x + y == y + x = 1
    | otherwise = 1
applyInt f x = f (x+1) +1
apply2 f x y = f y x

infinito = infinito +1
indefinido x = infinito +x
