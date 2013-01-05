function dist = ZipfianDist(numOfElements, coefficient)

dist = 1./ (1:numOfElements).^coefficient;

dist = dist / sum(dist);

end

