function predictions = barzanKccaInvoke(model, features)

K = 2;

temp = km_kernel(features, model.X, model.kernel, model.kernelpar);
projectedTestX = temp * model.alphaX;

[idx distance] = knnsearch(model.projectedX, projectedTestX, 'K', K);

nTest = size(features, 1);
predictions = zeros(nTest, 1);

for i=1:nTest
    predictions(i) = mean(model.Y(idx(i,:)));
end

end

