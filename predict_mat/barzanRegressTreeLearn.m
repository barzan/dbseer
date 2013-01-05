function model = barzanRegressTreeLearn(responseVars, features)
% learns a compact regression tree

model = RegressionTree.fit(features, responseVars, 'MergeLeaves', 'on', 'MinLeaf', 1, 'Prune', 'off', 'MinParent', 10);%, 'kfold', 10);

%model = compact(tree);

end

