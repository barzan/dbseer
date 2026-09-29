% Vendored from the retired shared-helper repository, commit 8b6dd7d.
% The original helper did not include a copyright or license header.

function produceWekaFile( header, matrix, filename )
%UNTITLED5 Summary of this function goes here
%   Detailed explanation goes here

system(horzcat('echo -e ',header,' > ./', filename));
dlmwrite(horzcat('./', filename), matrix, 'delimiter',',','-append','precision',10);

end
