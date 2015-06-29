% Copyright 2013 Barzan Mozafari
%
% Licensed under the Apache License, Version 2.0 (the "License");
% you may not use this file except in compliance with the License.
% You may obtain a copy of the License at
%
%     http://www.apache.org/licenses/LICENSE-2.0
%
% Unless required by applicable law or agreed to in writing, software
% distributed under the License is distributed on an "AS IS" BASIS,
% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
% See the License for the specific language governing permissions and
% limitations under the License.

config_dir = 'tpcc4-redo/';
config_signature = 't12';

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

    % test neural network in Octave
    nnModel_Octave = barzanNeuralNetLearn(trainPagesFlushed, trainC);
    nnPred_Octave = barzanNeuralNetInvoke(nnModel_Octave, testC);
    
    [rel_err_nn_octave(i) abs_err_nn_octave(i) rel_diff_nn_octave(i) discrete_rel_error_nn_octave(i) weka_rel_err] = myerr(nnPred_Octave, testPagesFlushed);
end

% save the relative error to MAT.
save('-v6', 'rel_err_nn_octave.mat', 'rel_err_nn_octave');



