function barzanRegressTreeLearn(responseVars, features)
	#% learns a compact regression tree
	features=features''
	model = build_tree(vec(responseVars),features,5)
	#model = RegressionTree.fit(features, responseVars, 'MergeLeaves', 'on', 'MinLeaf', 1, 'Prune', 'off', 'MinParent', 10);

	#%save('RegresstionTree_Model', 'model')
	#%load('RegresstionTree_Model', 'model')
	#%disp 'RTREE PAUSE'
	##%pause
	#%model = compact(tree);
	return model
end
