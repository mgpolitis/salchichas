function U=forwdif(f,c1,c2,a,b,c,n,m)

% solves heat equation u_t = c*u_xx

% Input	
% 	- f = u(x,0) as a string 'f'
%	- c1 = u(0,t) and c2 = u(a,t)
% 	- a and b, right endpoints of x in [0,a], t in [0,b]
% 	- c the constant in the heat equation
% 	- n and m numbero of grid points over [0,a] and [0,b]
% Output
% 	- U solution matrix


% Initialize parameters and U
h = a/(n-1);
k = b/(m-1);
r = c^2*k/h^2
s=1-2*r;
U=zeros(n,m);

% Boundary conditions
U(1,1:m)=c1;
U(n,1:m)=c2;

% Generate first rows
U(2:n-1,1)=feval(f,h:h:(n-2)*h)';

% Generate remaining rows of U
for j=2:m
	for i=2:n-1
		U(i,j)=s*U(i,j-1)+r*(U(i-1,j-1)+U(i+1,j-1));
	end
end
U=U';
