function u = discretizations()

% f'(x) = \dfrac{f(x+h)-f(x)}{h}  + 1/2 * h * f''(c) [O(h)]
% f'(x) = \dfrac{f(x+h)-f(x-h)}{2h}  + 1/12 * h^2 * f'''(c) [O(h^2)]

% f''(x) = \dfrac{f(x-h) -2*f(x) +f(x-h)}{h^2} + 1/24 * h^2 * f''''(c)
u=0;
end
