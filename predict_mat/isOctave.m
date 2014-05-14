function ret = isOctave
    if exist('OCTAVE_VERSION')
        ret = 1;
    else
        ret = 0;
    end
end
