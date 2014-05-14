function predictions = barzanRegressTreeInvoke(model, features, use_octave)
% Invokes the regression tree for the given set of features
if nargin < 3
    use_octave = 0;
end

if isOctave || use_octave==1
    test_size = size(features);
    for i = 1:test_size(1)
        predictions(i,1) = m5ppredict(model, features(i,:));
    end
else
    predictions = predict(model, features);
end

end

