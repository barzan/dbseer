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

## Copyright (C) 2013 Erik Kjellson <erikiiofph7@users.sourceforge.net>
##
## This program is free software; you can redistribute it and/or modify it under
## the terms of the GNU General Public License as published by the Free Software
## Foundation; either version 3 of the License, or (at your option) any later
## version.
##
## This program is distributed in the hope that it will be useful, but WITHOUT
## ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
## FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
## details.
##
## You should have received a copy of the GNU General Public License along with
## this program; if not, see <http://www.gnu.org/licenses/>.
 
## -*- texinfo -*-
## @deftypefn  {Function File} {@var{yy} =} smooth (@var{y})
## @deftypefnx {Function File} {@var{yy} =} smooth (@var{y}, @var{span})
## @deftypefnx {Function File} {@var{yy} =} smooth (@var{y}, @var{method})
## @deftypefnx {Function File} {@var{yy} =} smooth (@var{y}, @var{span}, @var{method})
## @deftypefnx {Function File} {@var{yy} =} smooth (@var{y}, "sgolay", @var{degree})
## @deftypefnx {Function File} {@var{yy} =} smooth (@var{y}, @var{span}, 'sgolay', @var{degree})
## @deftypefnx {Function File} {@var{yy} =} smooth (@var{x}, @var{y}, ...)
##
## This is an implementation of the functionality of the @code{smooth} function in 
## Matlab's Curve Fitting Toolbox.
##
## Smooths the @var{y} data with the chosen method, see the table below for available 
## methods.
##
## The @var{x} data does not need to have uniform spacing.
##
## For the methods "moving" and "sgolay" the @var{span} parameter defines how many data 
## points to use for the smoothing of each data point. Default is 5, i.e. the 
## center point and two neighbours on each side.
##
## Smoothing methods specified by @var{method}:
## 
## @table @asis
## @item "moving"
## Moving average (default). For each data point, the average value of the span
## is used. Corresponds to lowpass filtering.
## 
## @item "sgolay"
## Savitzky-Golay filter. For each data point a polynomial of degree @var{degree}
## is fitted (using a least-square regression) to the span and evaluated for the 
## current @var{x} value. Also known as digital smoothing polynomial filter or 
## least-squares smoothing filter. Default value of @var{degree} is 2.
## 
## @item "lowess"
## 
## @item "loess"
## 
## @item "rlowess"
## 
## @item "rloess"
## 
## @end table
##
## Documentation of the Matlab smooth function:
##   @url{http://www.mathworks.se/help/curvefit/smooth.html}
##   @url{http://www.mathworks.se/help/curvefit/smoothing-data.html}
##
## @end deftypefn

function yy = OctSmooth (varargin)
  
  ## Default values
  
  span   = 5;
  method = 'moving';
  degree = 2;        ## for sgolay method
  
  ## Keep track of the order of the arguments
  argidx_x      = -1;
  argidx_y      = -1;
  argidx_span   = -1;
  argidx_method = -1;
  argidx_degree = -1;
  
  ## Check input arguments
  
  if (nargin < 1)
    print_usage ();
  else
    ## 1 or more arguments
    if (!isnumeric (varargin{1}))
      error ('smooth: first argument must be a vector')
    endif
    if (nargin < 2)
      ## first argument is y
      argidx_y = 1;
      y = varargin{1};
    else
      ## 2 or more arguments
      if ((isnumeric (varargin{2})) && (length (varargin{2}) > 1))
        ## both x and y are provided
        argidx_x = 1;
        argidx_y = 2;
        x = varargin{1};
        y = varargin{2};
        if (length (x) != length (y))
          error ('smooth: x and y vectors must have the same length')
        endif
      else
        ## Only y provided, create an evenly spaced x vector
        argidx_y = 1;
        y    = varargin{1};
        x = 1:length (y);
        if ((isnumeric (varargin{2})) && (length (varargin{2}) == 1))
          ## 2nd argument is span
          argidx_span = 2;
          span = varargin{2};
        elseif (ischar (varargin{2}))
          ## 2nd argument is method
          argidx_method = 2;
          method = varargin{2};        
        else
          error ('smooth: 2nd argument is of unexpected type')
        endif
      endif
      if (nargin > 2)
        if ((argidx_y == 2) && (isnumeric (varargin{3})))
          ## 3rd argument is span
          argidx_span = 3;
          span = varargin{3};
          if (length (span) > 1)
            error ('smooth: 3rd argument can''t be a vector')
          endif
        elseif (ischar (varargin{3}))
          ## 3rd argument is method
          argidx_method = 3;
          method = varargin{3};   
        elseif (strcmp (varargin{2}, 'sgolay') && (isnumeric (varargin{3})))
          ## 3rd argument is degree
          argidx_degree = 3;
          degree = varargin{3};
          if (length (degree) > 1)
            error ('smooth: 3rd argument is of unexpected type')
          endif
        else
          error ('smooth: 3rd argument is of unexpected type')
        endif
        if (nargin > 3)
          if (argidx_span == 3)
            ## 4th argument is method
            argidx_mehod = 4;
            method = varargin{4};
            if (!ischar (method))
              error ('smooth: 4th argument is of unexpected type')
            endif
          elseif (strcmp (varargin{3}, 'sgolay'))
            ## 4th argument is degree
            argidx_degree = 4;
            degree = varargin{4};
            if ((!isnumeric (degree)) || (length (degree) > 1))
              error ('smooth: 4th argument is of unexpected type')
            endif
          else
            error ('smooth: based on the first 3 arguments, a 4th wasn''t expected')
          endif
          if (nargin > 4)
            if (strcmp (varargin{4}, 'sgolay'))
              ## 5th argument is degree
              argidx_degree = 5;
              degree = varargin{5};
              if ((!isnumeric (degree)) || (length (degree) > 1))
                error ('smooth: 5th argument is of unexpected type')
              endif                
            else
              error ('smooth: based on the first 4 arguments, a 5th wasn''t expected')
            endif
            if (nargin > 5)
              error ('smooth: too many input arguments')
            endif
          endif
        endif
      endif
    endif
  endif

  
  ## Perform smoothing
  
  if (span > length (y))
      span = length(y);
%    error ('smooth: span cannot be greater than ''length (y)''.')
  endif
  yy = [];
  switch method
    ## --- Moving average
    case 'moving'
      for i=1:length (y)
          span = span-1+mod(span,2); % forces it to be odd.
        %if (mod (span,2) == 0)
        %  error ('smooth: span must be odd.')
        %endif
        if (i <= (span-1)/2)
          ## We're in the beginning of the vector, use as many y values as 
          ## possible and still having the index i in the center.
          ## Use 2*i-1 as the span.
          idx1 = 1;
          idx2 = 2*i-1;
        elseif (i <= length (y) - (span-1)/2)
          ## We're somewhere in the middle of the vector.
          ## Use full span.
          idx1 = i-(span-1)/2;
          idx2 = i+(span-1)/2;
        else
          ## We're near the end of the vector, reduce span.
          ## Use 2*(length (y) - i) + 1 as span
          idx1 = i - (length (y) - i);
          idx2 = i + (length (y) - i);
        endif
        yy(i) = mean (y(idx1:idx2));
      endfor
      
    ## --- Savitzky-Golay filtering
    case 'sgolay'
      ## FIXME: Check how Matlab takes care of the beginning and the end. Reduce polynomial degree?
      for i=1:length (y)
        if (mod (span,2) == 0)
          error ('smooth: span must be odd.')
        endif
        if (i <= (span-1)/2)
          ## We're in the beginning of the vector, use as many y values as 
          ## possible and still having the index i in the center.
          ## Use 2*i-1 as the span.
          idx1 = 1;
          idx2 = 2*i-1;
        elseif (i <= length (y) - (span-1)/2)
          ## We're somewhere in the middle of the vector.
          ## Use full span.
          idx1 = i-(span-1)/2;
          idx2 = i+(span-1)/2;
        else
          ## We're near the end of the vector, reduce span.
          ## Use 2*(length (y) - i) + 1 as span
          idx1 = i - (length (y) - i);
          idx2 = i + (length (y) - i);
        endif
        ## Fit a polynomial to the span using least-square method.
        p     = polyfit(x(idx1:idx2), y(idx1:idx2), degree);
        ## Evaluate the polynomial in the center of the span.
        yy(i) = polyval(p,x(i));
      endfor
            
    ## ---
    case 'lowess'
      ## FIXME: implement smoothing method 'lowess'
      error ('smooth: method ''lowess'' not implemented yet')
      
    ## ---
    case 'loess'
      ## FIXME: implement smoothing method 'loess'
      error ('smooth: method ''loess'' not implemented yet')
      
    ## ---
    case 'rlowess'
      ## FIXME: implement smoothing method 'rlowess'
      error ('smooth: method ''rlowess'' not implemented yet')
      
    ## ---
    case 'rloess'
      ## FIXME: implement smoothing method 'rloess'
      error ('smooth: method ''rloess'' not implemented yet')
      
    ## ---
    otherwise
      error ('smooth: unknown method')
  endswitch
endfunction


########################################

%!test
%! ## 5 y values (same as default span)
%! y = [42 7 34 5 9];
%! yy2    = y;
%! yy2(2) = (y(1) + y(2) + y(3))/3;
%! yy2(3) = (y(1) + y(2) + y(3) + y(4) + y(5))/5;
%! yy2(4) = (y(3) + y(4) + y(5))/3;
%! yy = smooth (y);
%! assert (yy, yy2);

%!test
%! ## x vector provided
%! x = 1:5;
%! y = [42 7 34 5 9];
%! yy2    = y;
%! yy2(2) = (y(1) + y(2) + y(3))/3;
%! yy2(3) = (y(1) + y(2) + y(3) + y(4) + y(5))/5;
%! yy2(4) = (y(3) + y(4) + y(5))/3;
%! yy = smooth (x, y);
%! assert (yy, yy2);

%!test
%! ## span provided
%! y = [42 7 34 5 9];
%! yy2    = y;
%! yy2(2) = (y(1) + y(2) + y(3))/3;
%! yy2(3) = (y(2) + y(3) + y(4))/3;
%! yy2(4) = (y(3) + y(4) + y(5))/3;
%! yy = smooth (y, 3);
%! assert (yy, yy2);

%!test
%! ## x vector & span provided
%! x = 1:5;
%! y = [42 7 34 5 9];
%! yy2    = y;
%! yy2(2) = (y(1) + y(2) + y(3))/3;
%! yy2(3) = (y(2) + y(3) + y(4))/3;
%! yy2(4) = (y(3) + y(4) + y(5))/3;
%! yy = smooth (x, y, 3);
%! assert (yy, yy2);

%!test
%! ## method 'moving' provided
%! y = [42 7 34 5 9];
%! yy2    = y;
%! yy2(2) = (y(1) + y(2) + y(3))/3;
%! yy2(3) = (y(1) + y(2) + y(3) + y(4) + y(5))/5;
%! yy2(4) = (y(3) + y(4) + y(5))/3;
%! yy = smooth (y, 'moving');
%! assert (yy, yy2);

%!test
%! ## x vector & method 'moving' provided
%! x = 1:5;
%! y = [42 7 34 5 9];
%! yy2    = y;
%! yy2(2) = (y(1) + y(2) + y(3))/3;
%! yy2(3) = (y(1) + y(2) + y(3) + y(4) + y(5))/5;
%! yy2(4) = (y(3) + y(4) + y(5))/3;
%! yy = smooth (x, y, 'moving');
%! assert (yy, yy2);

%!test
%! ## span & method 'moving' provided
%! y = [42 7 34 5 9];
%! yy2    = y;
%! yy2(2) = (y(1) + y(2) + y(3))/3;
%! yy2(3) = (y(2) + y(3) + y(4))/3;
%! yy2(4) = (y(3) + y(4) + y(5))/3;
%! yy = smooth (y, 3, 'moving');
%! assert (yy, yy2);

%!test
%! ## x vector, span & method 'moving' provided
%! x = 1:5;
%! y = [42 7 34 5 9];
%! yy2    = y;
%! yy2(2) = (y(1) + y(2) + y(3))/3;
%! yy2(3) = (y(2) + y(3) + y(4))/3;
%! yy2(4) = (y(3) + y(4) + y(5))/3;
%! yy = smooth (x, y, 3, 'moving');
%! assert (yy, yy2);

########################################

%!demo
%! ## Moving average & Savitzky-Golay
%! x     = linspace (0, 4*pi, 150);
%! y     = sin (x) + 1*(rand (1, length (x)) - 0.5);
%! y_ma  = smooth (y, 21, 'moving');
%! y_sg  = smooth (y, 21, 'sgolay', 2);
%! y_sg2 = smooth (y, 51, 'sgolay', 2);
%! figure
%! plot (x,y, x,y_ma, x,y_sg, x,y_sg2)
%! legend('Original', 'Moving Average (span 21)', 'Savitzky-Golay (span 21, degree 2)', 'Savitzky-Golay (span 51, degree 2)')

