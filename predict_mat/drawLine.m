function ph = drawLine(HorzOrVert, ColorStyle, value, caption)

ax = gca;
xlim = get(ax, 'XLim');
xmin = xlim(1); xmax=xlim(2);
ylim = get(ax, 'YLim');
ymin = ylim(1); ymax=ylim(2);

s=50;

if HorzOrVert=='h'
    vals = [(xmin:s:xmax)' repmat(value, size((xmin:s:xmax)'))];
else
    if HorzOrVert=='v'
        vals = [repmat(value, size((ymin:s:ymax)')) (ymin:s:ymax)'];
    else
        error('Invalid HorzOrVert!');
    end
end
hold on;    
ph = plot(vals(:,1), vals(:,2), ColorStyle, 'DisplayName', caption);

end

