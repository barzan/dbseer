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

function [tps_ J_ f_ K_ regions_ D_ S0_ S_ g_] = prepareLockModelNew(datasetPath, counts)
% function [g S K readRows updateRows] = prepareLockModelNew(datasetPath, counts)

path = datasetPath.path;

query_stat_struct = dir([path '/latency_sample_*']);
query_stat_files = {};
query_stat = {};
num_tx_type = size(query_stat_struct, 1);

J = num_tx_type;
f = ones(1, J);
f = f ./ sum(f);

% read table row counts
table_row_count = readtable([path '/table_row_count']);
regions = size(table_row_count, 1);
table_idx = 1:regions;

% create a map which maps table name with its index
table_map = containers.Map(table_row_count.Var1, table_idx);

D = zeros(1, regions);
for i=1:regions
  D(i) = table_row_count.Var2(i);
end

readRows = zeros(J, regions);
updateRows = zeros(J, regions);
latencies = {};
tx_stats = {};

% SELECT = 1;
% INSERT = 2;
% UPDATE = 3;
% DELETE = 4;

K = [];

for i=1:num_tx_type
  query_stat_files{i} = query_stat_struct(i).name;
  % query_stat{i} = csvread([path '/' query_stat_files{i}]);
  file = textread([path '/' query_stat_files{i}], '%s\n');
  last_tx_id = -1;
  count = 0;
  num_tx = 0;
  tx_stat = {};
  for j=1:size(file,1)
    stat = strsplit(file{j}, ',');
    tx_id = str2double(stat{1});
    latency = str2double(stat{3});
    op = str2double(stat{4});
    num_table = str2double(stat{5});
    if tx_id == last_tx_id
      count = count + 1;
    else
      count = 1;
      num_tx = num_tx + 1;
    end
    last_tx_id = tx_id;

    if size(tx_stat, 2) < count
      tx_stat{count} = struct;
      tx_stat{count}.latency = 0;
      tx_stat{count}.num_statement = 0;
      tx_stat{count}.access_count = zeros(1, regions);
      tx_stat{count}.read_rows = zeros(1, regions);
      tx_stat{count}.update_rows = zeros(1, regions);
    end

    tx_stat{count}.latency = tx_stat{count}.latency + latency;
    tx_stat{count}.num_statement = tx_stat{count}.num_statement + 1;

    for k=1:num_table
      table_name = stat{5+k};
      rows = str2double(stat{5+num_table+k});
      tx_stat{count}.access_count(table_map(table_name)) = tx_stat{count}.access_count(table_map(table_name)) + 1;
      if op == 1
        readRows(i, table_map(table_name)) = readRows(i, table_map(table_name)) + rows;
      else
        updateRows(i, table_map(table_name)) = updateRows(i, table_map(table_name)) + rows;
      end
    end
  end
  for k=1:regions
    readRows(i, k) = ceil(readRows(i, k) / num_tx);
    updateRows(i, k) = ceil(updateRows(i, k) / num_tx);
  end
  tx_stats{i,1} = tx_stat;
  tx_stats{i,2} = num_tx;
  K(i) = size(tx_stats{i,1}, 2);
end

maxK = max(K);
g = zeros([J maxK regions], 'double');
S = zeros(J,maxK, 'double');
S0 = zeros(1, J);

for i=1:J
  for j=1:K(i)
    for k=1:regions
      g(i,j,k) = tx_stats{i,1}{j}.access_count(k) / tx_stats{i,1}{j}.num_statement;
      S(i,j) = tx_stats{i,1}{j}.latency / tx_stats{i,1}{j}.num_statement * 1000 * 1000; % convert to nanoseconds
    end
    g(i,j,:) = g(i,j,:) ./ sum(g(i,j,:));
    
  end
end


f = counts;
tps = sum(f,2);
f = f / tps;

b = zeros(1, regions);
for i=1:regions
  sm = (readRows(:,i)+updateRows(:,i));
  sm(sm==0) = 123456;
  b(i) = dot(f, (updateRows(:,i) ./ sm)');
end
b(b==0)=0.0000000100; %DomainMultiplier;
D = D ./ (1- (1-b.*b)); % when we do have exclusive locks we should not shrink all the tables!
tps_ = tps;
J_ = J;
f_ = f;
K_ = K;
regions_ = regions;
D_ = D;
S0_ = S0;
S_ = S;
g_ = g;

end % end function
