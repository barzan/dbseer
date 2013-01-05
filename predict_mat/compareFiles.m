function compareFiles(jump, varargin)

dim1 = [1 1 3 2 2 2 3 3 3 3 3 3 4 4 4 4];
dim2 = [1 2 1 2 3 3 3 3 3 4 4 4 4 4 4 4];

num = size(varargin,2);
%D1 = dim1(num*jump);
%D2 = dim2(num*jump);
D1 = num;
D2 = jump;


screen_size = get(0, 'ScreenSize');
fh = figure('Name','?!');
set(fh, 'Position', [0 0 screen_size(3) screen_size(4)]);

for i=1:num
    load_and_plot(horzcat('monitor-',varargin{i}),horzcat('trans-',varargin{i}), 1+(i-1)*jump, D1, D2);
    for j=1:jump
        subplot(D1, D2, j+(i-1)*jump);
        title(varargin{i});
        if i>1
            legend('off');
        end
    end
end

end
