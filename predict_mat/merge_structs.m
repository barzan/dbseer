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

function [ mv3 ] = merge_structs( mv1, mv2, compare_header_equality )

mv3 = struct();
fn1 = fieldnames(mv1);
fn2 = fieldnames(mv2);


for i=1:length(fn1)
    found = false;
    for j=1:length(fn2)
      if strcmpi(fn1{i}, fn2{j})
        A = eval(['mv1.' fn1{i}]);
        B = eval(['mv2.' fn1{i}]);
        if size(A,1)==1
            if isstruct(A) && isstruct(B)
                if nargin==3
                    C = merge_structs(A, B, compare_header_equality);
                else
                    C = merge_structs(A, B);
                end
                eval(['mv3.' fn1{i} ' = C;']);
            elseif all(A==B)==1
                eval(['mv3.' fn1{i} ' = mv1.' fn1{i} ';']);
            elseif strcmpi(fn1{i}, 'numberOfObservations')
                eval(['mv3.' fn1{i} ' = mv1.' fn1{i} ' + mv2.' fn1{i} ';']);
            else
                error(['Variable values do not match!']);
            end
        else
            str = ['mv3.' fn1{i} ' = [mv1.' fn1{i} '; mv2.' fn1{i} '];'];
            eval(str);
        end
        found = true;
      end
    end
    if nargin==3 && compare_header_equality && ~found
        error(['Variable ' fn1{i} ' not found in the other struct!']);
    end
end

end