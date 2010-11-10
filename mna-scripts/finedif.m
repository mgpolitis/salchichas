function U = finedif(f,g,a,b,c,n,m)

% solves the wave equation u_tt = c*u_xx


%Input    - f=u(x,0) 
%            - g=ut(x,0) 
%            - a and b right endpoints of [0,a] and [0,b]
%            - c the constant in the wave equation
%            - n and m number of grid points over [0,a] and [0,b]
%Output - U solution matrix; analogous to Table 10.1

% exact, d'alambert:
% u(x,t) = [F(x+ct) + F(x-ct)]/2 + 1/2c \int {x-ct}{x+ct} G(z)dz

% If f and g are M-file functions call U = finedif(@f,@g,a,b,c,n,m).
% if f and g are anonymous functions call U = finedif(f,g,a,b,c,n,m).


%Initialize parameters and U

h = a/(n-1);
k = b/(m-1);
r = c*k/h;
r2=r^2;
r22=r^2/2;
s1 = 1 - r^2;
s2 = 2 - 2*r^2;
U = zeros(n,m);

%Comput first and second rows

for i=2:n-1
   U(i,1)=f(h*(i-1));
   U(i,2)=s1*f(h*(i-1))+k*g(h*(i-1)) ...
          +r22*(f(h*i)+f(h*(i-2)));
end
    
%Compute remaining rows of U

for j=3:m,
  for i=2:(n-1),
     U(i,j) = s2*U(i,j-1)+r2*(U(i-1,j-1)+U(i+1,j-1))-U(i,j-2);
  end
end

U=U';

