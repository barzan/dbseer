function showList(node)

while ~isempty(node)
    if node.belowMiddle
        fprintf(1, '%d (true) => ', node.Data);
    else
        fprintf(1, '%d (false) => ', node.Data);
    end
    node = node.getNext;
end

fprintf(1,'\n');

end

