function u = eigenvalues()

% A = C_{BE} * D * C_{EB}
% C_{BE}^{-1} = C_{EB}


% w = x-y/|x-y|2
% P = I - 2 w w'

% Ak = Pk Ak-1 Pk

% faster:
% V = Aw
% c = w'V
% Q = V - cw
% PAP = A -2wQ'-2Qw'

% Ak = Qk * Uk
% Ak = Uk * Qk

u=0;
end
