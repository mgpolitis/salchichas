function X = trisys(A,D,C,B)
%---------------------------------------------------------------------------
%TRISYS   Solution of a triangular linear system.
%         It is assumed that D and B have dimension n,
%         and that A and C have dimension n-1;
% Sample call
%   X = trisys(A,D,C,B)
% Inputs
%   A   sub diagonal vector
%   D   diagonal vector
%   C   super diagonal vector
%   B   right hand side vector
% Return
%   X   solution vector
%

n = length(B);
for k = 2:n,
  mult = A(k-1)/D(k-1);
  D(k) = D(k) - mult*C(k-1);
  B(k) = B(k) - mult*B(k-1);
end
X(n) = B(n)/D(n);
for k = (n-1):-1:1,
  X(k) = (B(k) - C(k)*X(k+1))/D(k);
end
