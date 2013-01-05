function C = div(A, B )
    C = A ./ B;
    C(B==0 & A==0) = 0;
end

