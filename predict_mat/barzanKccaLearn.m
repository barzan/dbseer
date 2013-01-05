function model = barzanKccaLearn(responseVars, features)

kernel = 'gauss';
kernelpar = 1;
[y1,y2,alpha1,alpha2,K1,K2,beta] = km_kcca_barzan(features, responseVars, kernel, kernelpar, 'euclid', [], 1E-5);

model = struct('projectedX', y1, 'projectedY', y2, 'X', features, 'Y', responseVars, 'alphaX', alpha1, 'alphaY', alpha2, 'KX', K1, 'KY', K2, 'beta', beta, 'kernel', kernel, 'kernelpar', kernelpar);

end

