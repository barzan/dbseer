function outStruct = rmfield_safe(myStruct, fieldname)
    if isfield(myStruct, fieldname)
        outStruct = rmfield(myStruct, fieldname);
    else
        outStruct = myStruct;
    end
end

