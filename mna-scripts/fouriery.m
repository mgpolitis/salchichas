function u=fouriery()

% w0 = 2*pi/T

% f(t) = a0/2 + \sum_{1}{8} an*cos(n*w0*t)+ bn*sin(n*w0*t) 
%
% a0/2 = 1/T \int_{-T/2}^{T/2} f(t) dt
% an   = 2/T \int_{-T/2}^{T/2} f(t)*cos(n*w0*t) dt
% bn   = 2/T \int_{-T/2}^{T/2} f(t)*sin(n*w0*t) dt
%
%
%
% f(t) = \sum_{-8}{8}cn*exp(i*n*w0*t)
% cn   = 1/T * \int_{-T/2}^{T/2} f(t) * exp(-i*n*w0*t) dt
% 
% 
% parseval
% 1/T \int_{-T/2}^{T/2} |f(t)|^2 dt = \sum{-8}{8} |cn|^2
% a0^2/2 + \sum an^2+bn^2 = 1/L \int{c}{c+2L}f(x)dx






%
% \hat f (w) = 1/2pi \int{-8}{8} f(t) exp(-i*w*t) dt
% ift(t) = \int{-8}{8} f(w) exp(i*w*t) dw
%
% bessell (parseval 2)
% \int{-8}{8}|\hat f(w)|^2 dw = 1/2pi \int{-8}{8} |f(x)|^2 dx
%

% d(t-a) "=" 1/2pi * \int{-8}{8}exp(-i*x*(t-a)) dx


% \hat f_mn = \sum_k \sum_l f_kl exp(-i*2*pi*(km/M+nl/N))
% idft f_mn = \sum_k \sum_l f_kl exp(i*2*pi*(km/M+nl/N))


u=0;
end
