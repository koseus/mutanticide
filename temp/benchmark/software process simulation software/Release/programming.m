function program_structure  = programming (program_structure, programming_time, varargin)

% Execute a programming step
%
% Required inputs:
%   program_so_far      - A programming structure of the programming so far
%   programming_time    - The time that the step will be carried out
%
% Optional inputs (defaults):
%   lines_per_hour      - Lines of programming added per hour (30)
%   bugs_per_line       - The number of bugs per line of code (0.01)
%   bugs                - The bug prototype that will be used
%   complexity_function - The reduction in effective time due to complexity,
%                         in the format 'fun(x)' where x is a time value (x)
%
% Output:
%   program_structure   - A modified program structure
%
% Change the defaults using the pair: 'param', param value

bugs                = bug;
lines_per_hour      = 30;
bugs_per_line       = 0.01;
complexity_function = '(x)';

possible_params         = {'lines_per_hour', 'bugs_per_line', 'bugs', 'complexity_function'};

for i = 1:floor(length(varargin)/2)
    param   = varargin{i*2-1};
    if any(ismember(param, possible_params))
        value   = varargin{i*2};
        if isnumeric(value)
            eval([param ' = ' num2str(value) ';']);
        else
            eval([param ' = ''' value ''';']);
        end
    else
        error(['Unknown parameter: ' param])
    end
end

%Increment time
program_structure.time  = program_structure.time + 1;

%Re-estimate time
new_programming_time    = eval(strrep(complexity_function, 'x', 'programming_time'));
if (new_programming_time > programming_time)
    warning('Complexity function increased the effective time!');
end
programming_time    = new_programming_time;

% How many lines will be added this time?
new_lines   = round(lines_per_hour * programming_time);

% Now add the bugs (FUN!)
new_bugs    = round(new_lines * bugs_per_line);
bug_fields  = fields(bugs);
for i = 1:new_bugs
    %Where?
    cur_line    = max(1, min(new_lines, round(rand(1)*new_lines)));
    
    %Insert
    Nbug        = length(program_structure.bugs) + 1;
    program_structure.bugs(Nbug).line           = program_structure.lines + cur_line;
    program_structure.bugs(Nbug).insert_time    = program_structure.time;
    program_structure.bugs(Nbug).fixed          = 0;
    program_structure.bugs(Nbug).tests_run      = [];
	for j = 1:length(bug_fields)
        eval(['program_structure.bugs(Nbug).' bug_fields{j} '=bugs.' bug_fields{j} ';']);
    end
end

program_structure.lines = program_structure.lines + new_lines;