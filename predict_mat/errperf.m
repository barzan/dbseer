function V=errperf(T,P,M)

%ERRPERF Determine various error related performance metrics.
%
% ERRPERF(T,P,M) uses T and P, which are target and prediction vectors
% respectively, and returns the value for M, which is one of several error
% related performance metrics.
%
% T and P can be row or column vectors of the same size. M can be one of
% the following performance metrics:
%
%   mae (mean absolute error)
%   mse (mean squared error)
%   rmse (root mean squared error)
%
%   mare (mean absolute relative error)
%   msre (mean squared relative error)
%   rmsre (root mean squared relative error)
%
%   mape (mean absolute percentage error)
%   mspe (mean squared percentage error)
%   rmspe (root mean squared percentage error)
%
% EXAMPLE:
%
%   rand('state',0)
%
%   T = [0:0.2:1]
%   P = rand(size(T)).*T
%
%   errperf(T,P,'mae') returns 0.1574
%
% To compute the relevant performance metric, the function uses recursion
% to first compute one or more error vectors. The function can therefore
% secondarily be used to compute these error vectors. M can therefore also
% be one of the following:
%
%   e (errors)
%   ae (absolute errors)
%   se (squared errors)
%
%   re (relative errors)
%   are (absolute relative errors)
%   sre (squared relative errors)
%
%   pe (percentage errors)
%   ape (absolute percentage errors)
%   spe (squared percentage errors)
%
% REMARKS:
%
%   - The Neural Network Toolbox also has functions to compute mae and mse.
%     This function does not make use of the toolbox.
%
%   - Percentage error equals relative error times 100.
%
%   - The abbreviations used in the code, and the calculation tree are
%     documented in a comments section within the file.
%
% VERSION: 20070703
% MATLAB VERSION: 7.4.0.287 (R2007a)
% LICENSE: As-is; public domain
%
% See also MAE, MSE.

%{
VERSION HISTORY:
20070703: - Added MATLAB version check.
20070606: - Added support for metrics MARE, MSRE, and RMSRE.
          - Addressed a possible division by zero condition in calculating
            relative and percentage errors.
20070528: - Original version.

KEYWORDS:
perf, performance, metric, performance measure, machine learning
%}

%% Comments

%{

Abbreviations:

a: absolute
e: error(s)
M: METRIC
m: mean
P: PREDICTIONS
p: percentage
r: relative (if before e)
r: square root (if before m)
s: squared
T: TARGETS
V: VALUE(S)

Calculation tree:

e
|
|-ae-mae
|
|-se-mse-rmse
|
|-re-pe
  |  |
  |  |-ape-mape
  |  |
  |  |-spe-mspe-rmspe
  |
  |-are-mare
  |
  |-sre-msre-rmsre

%}

%% Check MATLAB version

if datenum(version('-date'))<datenum('29-Jan-2007')
    error(['The MATLAB version in use is ',version('-release'),'. ',...
           'This function requires at least version 2007a.'])
end

%% Parse and validate input

Inputs=inputParser;

Inputs.addRequired('T',@(x) isnumeric(x) && ndims(x)==2 && ...
                            (size(x,1)==1 || size(x,2)==1));
Inputs.addRequired('P',@(x) isnumeric(x) && ndims(x)==2 && ...
                            (size(x,1)==1 || size(x,2)==1));
Inputs.addRequired('M',@(x) ischar(x) && ~isempty(x));
                
Inputs.parse(T,P,M);
clear Inputs

assert(isequal(size(T),size(P)),'T and P must have the same size.')

%% Transform input

M=lower(M);

%% Compute metric

switch M
  
  % Errors
  case 'e'
    
    V=T-P;
    
  % Absolute errors
  case 'ae'
    
    Ve=errperf(T,P,'e');
    V=abs(Ve);
    
  % Mean absolute error
  case 'mae'
    
    Vae=errperf(T,P,'ae');
    V=mean(Vae);
	
  % Squared errors
  case 'se'
    
    Ve=errperf(T,P,'e');
    V=Ve.^2;
  
  % Mean squared error
  case 'mse'
    
    Vse=errperf(T,P,'se');
    V=mean(Vse);
  
  % Root mean squared error
  case 'rmse'

    Vmse=errperf(T,P,'mse');
    V=sqrt(Vmse);
    
  % Relative errors
  case 're'
    
    assert(all(T),'All elements of T must be nonzero.')
    Ve=errperf(T,P,'e');
    V=Ve./T;
    
  % Absolute relative errors
  case 'are'
    
    Vre=errperf(T,P,'re');
    V=abs(Vre);
  
  % Mean absolute relative error
  case 'mare'
    
    Vare=errperf(T,P,'are');
    V=mean(Vare);
  
  % Squared relative errors
  case 'sre'
    
    Vre=errperf(T,P,'re');
    V=Vre.^2;
  
  % Mean squared relative error
  case 'msre'
    
    Vsre=errperf(T,P,'sre');
    V=mean(Vsre);
  
  % Root mean squared relative error
  case 'rmsre'
    
    Vmsre=errperf(T,P,'msre');
    V=sqrt(Vmsre);
  
  % Percentage errors
  case 'pe'
    
    Vre=errperf(T,P,'re');
    V=Vre*100;
    
  % Absolute percentage errors
  case 'ape'
    
    Vpe=errperf(T,P,'pe');
    V=abs(Vpe);
    
  % Mean absolute percentage error
  case 'mape'
    
    Vape=errperf(T,P,'ape');
    V=mean(Vape);
    
  % Squared percentage errors
  case 'spe'
    
    Vpe=errperf(T,P,'pe');
    V=Vpe.^2;
    
  % Mean squared percentage error
  case 'mspe'
    
    Vspe=errperf(T,P,'spe');
    V=mean(Vspe);
    
  % Root mean squared percentage error
  case 'rmspe'
    
    Vmspe=errperf(T,P,'mspe');
    V=sqrt(Vmspe);
    
  otherwise
    
    error('M is invalid.')
    
end
