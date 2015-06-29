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

inputFolder = '/home/curino/expr5/coefs/';
outputFolder = '/home/curino/expr5/coefs/cleaned/';

filePattern = fullfile(inputFolder, 'coefs*');
f = dir(filePattern);
for k = 1:length(f)
  baseFileName = f(k).name;
  fullFileName = fullfile(inputFolder, baseFileName);
  fprintf(1, 'processing %s\n', fullFileName);
  
  % call the deverticalize function for every file with 1 second window and
  % 5 variables
  fast_deverticalize(inputFolder, outputFolder, baseFileName,1,5);
  
end
