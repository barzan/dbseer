function model = barzanRegressTreeLearn(responseVars, features, use_octave)
% learns a compact regression tree
if nargin < 3
    use_octave = 0;
end

if isOctave || use_octave==1
    m5Parameters = m5pparams(false, 10, false);
    [model m5BuildTime] = m5pbuild(features, responseVars, m5Parameters);
else
    model = RegressionTree.fit(features, responseVars, 'MergeLeaves', 'on', 'MinLeaf', 1, 'Prune', 'off', 'MinParent', 10);%, 'kfold', 10);
end

%save('RegresstionTree_Model', 'model')
%load('RegresstionTree_Model', 'model')
%disp 'RTREE PAUSE'
%pause
%model = compact(tree);

end

