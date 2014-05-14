% calls OctSmooth for Octave, smooth for Matlab.
function Z = DoSmooth(varargin)
    if isOctave
        Z = OctSmooth(varargin{:});
        Z = Z';
    else
        Z = smooth(varargin{:});
    end
end
    