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

function [groupedMatrix freuqencies] = BetterGroupByAvg(matrix, keyIndices, howManyClusters, minFreq, minKeyValue, maxKeyValue)
    nClusters = howManyClusters;

    idx = find(sum(matrix(:,keyIndices),2)>=minKeyValue);
    matrix = matrix(idx,:);

    idx = find(sum(matrix(:,keyIndices),2)<=maxKeyValue);
    matrix = matrix(idx,:);

    key = matrix(:,keyIndices);

    %mySeed = 1363;
    %RandStream.setGlobalStream(RandStream('mt19937ar','seed', mySeed));
    %[IDX Centroids] = kmeans(key, nClusters, 'emptyaction','drop');
    if isOctave
        pkg load statistics;
    end
    [IDX Centroids] = kmeans(key, nClusters, 'emptyaction','singleton');

    groupedMatrix = zeros(nClusters, size(matrix,2));
    freuqencies = zeros(nClusters,1);

    actualNClusters = 0;

    for i=1:nClusters
        cluster = matrix(IDX==i,:);
        if size(cluster,1) < minFreq
            continue
        end
        actualNClusters = actualNClusters + 1;
        groupedMatrix(actualNClusters,:) = mean(cluster);
        freuqencies(actualNClusters) = size(cluster,1);
    end
    groupedMatrix = groupedMatrix(1:actualNClusters,:);
    freuqencies = freuqencies(1:actualNClusters);

return;
