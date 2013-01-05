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
