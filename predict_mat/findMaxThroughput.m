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

function [xMaxThroughput yMaxThroughput] = findMaxThroughput(SubmittedTransactions)
    sm1 = 1000; %1000: lower part, 5000:middle
    sm2 = 10;
    mslope = 10; %lower this number sooner it declares a max throughput!

    sTPS = DoSmooth(SubmittedTransactions, sm1);
    %sTPS = smooth(SubmittedTransactions, sm1);
    %sTPS = SubmittedTransactions;

	% disabled for now.
    if isOctave
        % I = vl_localmax(sTPS');
        pkg load signal;
        try
            [Y I Ext] = findpeaks(sTPS, 'MinPeakDistance', 1, 'MinPeakWidth', 0);
            I = sort(I);
        catch
            I = [];
        end
    else
        [Y I] = localmax(sTPS', 1, false);
    end
	% [Y I] = localmax(sTPS', 1, false);

    xMaxThroughput = I(find(diff(DoSmooth(I,sm2))<mslope, 1, 'first') + 1);
    yMaxThroughput = sTPS(xMaxThroughput);

end
