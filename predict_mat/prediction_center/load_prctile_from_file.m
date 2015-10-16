function latenciesPCtile = load_prctile_from_file(prctile_dir, latency)

log_size = size(latency, 1);
var_size = size(latency, 2) - 1;
latenciesPCtile = zeros(log_size, var_size + 1, 8);
prctiles = {};

prctile_files_struct = dir([prctile_dir '/prctile_latency_*']);
prctile_files = {};
for i=1:size(prctile_files_struct, 1)
    prctile_files{i} = prctile_files_struct(i).name;
    prctiles{i} = csvread([prctile_dir '/' prctile_files{i}]);
end

for i=1:log_size-1
    timestamp = latency(i,1);
    for j=1:var_size
        prctile = prctiles{j};
        prctileIndex = find(prctile==timestamp);
        if ~isempty(prctileIndex)
            latenciesPCtile(i,j+1,:) = prctile(prctileIndex, (2:9));
        end
    end
end
latenciesPCtile(:,1) = latency(:,1);

end