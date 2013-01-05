function vstr = valueToString( v )
    if ischar(v)
        vstr = ['''' v ''''];
    elseif isnumeric(v)
        if any(size(v)~=[1 1])
           vstr = '[';
           for i=1:size(v,1)
               for j=1:size(v,2)
                   vstr = [vstr num2str(v(i,j)) ' '];
               end
               vstr = [vstr '; '];
           end
           vstr = [vstr ']'];
        else
            vstr = num2str(v);
        end
    elseif islogical(v)
        if v
            vstr = 'true';
        else
            vstr = 'false';
        end
    elseif isstruct(v)
        vstr = 'struct(';
        fields = fieldnames(v);
        for i = 1:numel(fields)
            vstr = [vstr '''' fields{i} ''', '];
            w = v.(fields{i});
            if iscell(w)
                error('A cell cannot have a cell as a value, since it messes up the whole struct.');
            end
            vstr = [vstr valueToString(w)];
            if i<numel(fields)
                vstr = [vstr ', '];
            end
        end
        vstr = [vstr ')'];
    elseif iscell(v)
        vstr = '{';
        for i = 1:length(v)
            vstr = [vstr valueToString(v{i})];
            if i<length(v)
                vstr = [vstr ', '];
            end
        end
        vstr = [vstr '}'];        
    else
        error(['Add support for this type of object before passing it to valueToString:' class(v)]);
    end 
end

