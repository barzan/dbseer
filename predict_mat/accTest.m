init_pred_configs;

% set configs
train_configs = [Dt12345_brk_100_0_121_conf Dt12345_brk_600_0_1215_conf Dt12345_brk_100_0_121_conf Dt12345_brk_900_0_1498_conf];
test_configs = [Dt12345_brk_100_0_121_conf Dt12345_brk_100_0_121_conf Dt12345_brk_900_0_1498_conf Dt12345_brk_100_0_121_conf];

for i = 1:length(train_configs)
    train_config = train_configs(i);
    test_config = test_configs(i);
    
    train_config = rmfield_safe(train_config, 'groupingStrategy');
    tranTypes = test_config.tranTypes;

    % load modeling variables
    mvTrain = load_modeling_variables(train_config.dir, train_config.signature);
    mvTest = load_modeling_variables(test_config.dir, test_config.signature);

    % get transactions
    trainC = mvTrain.clientIndividualSubmittedTrans(:,tranTypes);
    testC = mvTest.clientIndividualSubmittedTrans(:,tranTypes);

    % actual flush rates
    trainPagesFlushed = mvTrain.dbmsFlushedPages;
    testPagesFlushed = mvTest.dbmsFlushedPages;

    % testing RegressionTree.fit (Matlab) vs. M5` (Octave/Matlab)
    treeModel_Matlab = barzanRegressTreeLearn(trainPagesFlushed, trainC, 0);
    treePred_Matlab = barzanRegressTreeInvoke(treeModel_Matlab, testC, 0);

    treeModel_Octave = barzanRegressTreeLearn(trainPagesFlushed, trainC, 1);
    treePred_Octave = barzanRegressTreeInvoke(treeModel_Octave, testC, 1);
    
    % test neural network in Matlab
    nnModel_Matlab = barzanNeuralNetLearn(trainPagesFlushed, trainC);
    nnPred_Matlab = barzanNeuralNetInvoke(nnModel_Matlab, testC);

    [rel_err_regtree(i) abs_err_regtree(i) rel_diff_regtree(i) discrete_rel_error_regtree(i) weka_rel_err] = myerr(treePred_Matlab, testPagesFlushed);
    [rel_err_m5p(i) abs_err_m5p(i) rel_diff_m5p(i) discrete_rel_error_m5p(i) weka_rel_err] = myerr(treePred_Octave, testPagesFlushed);
    [rel_err_nn_matlab(i) abs_err_nn_matlab(i) rel_diff_nn_matlab(i) discrete_rel_error_nn_matlab(i) weka_rel_err] = myerr(nnPred_Matlab, testPagesFlushed);
end

load('rel_err_nn_octave'); % assuming MAT file already generated from Octave.

% draw bar graph
bar_graph = bar(1:length(test_configs), [rel_err_regtree'*100 rel_err_m5p'*100 rel_err_nn_matlab'*100 rel_err_nn_octave'*100]);
grid on;
title('Accuracy comparison by releative error: RegressionTree.fit v. M5Prime v. MatlabNN v. OctaveNN', 'FontSize', 28);
ylabel('Percentage (%)', 'FontSize', 28);
labels = cell(1,4);
labels{1} = 'Train_100_Test_100';
labels{2} = 'Train_600_Test_100';
labels{3} = 'Train_100_Test_900';
labels{4} = 'Train_900_Test_100';
set(gca, 'xticklabels', labels);
set(gca, 'FontSize', 28);
legend('RegressionTree.fit', 'M5Prime', 'MatlabNN', 'OctaveNN');
