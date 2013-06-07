function [ nextStyle ] = nextPlotStyle( reset )

colors = {'b','r','g', 'm', 'y', 'c', 'k'};
symbols = {'*-', '+:', 'o', 'x', '.', 'v', '+', '^'};


persistent colorIdx;
persistent symbolIdx;

if (isempty(colorIdx) && isempty(symbolIdx)) || (nargin == 1 && reset==true)
    colorIdx = 1;
    symbolIdx = 1;  
else
    colorIdx = colorIdx + 1;
    symbolIdx = symbolIdx + 1;
end

if symbolIdx>length(symbols)
    symbolIdx = 1;
end
if colorIdx>length(colors)
    colorIdx = 1;
end

nextStyle = [colors{colorIdx} symbols{symbolIdx}];

end

