function [time_to_execute, program_structure]  = debugging (program_structure, bugs_found, varargin)

% Debug a program
%
% Required inputs:
%   program_so_far      - A programming structure of the programming so far
%   bugs_found          - A list of the bug numbers found
%
% Optional inputs (defaults):
%   time_to_fix         - Time to fix a bug (2)
%   lines_to_fix        - How many lines are changed when a bug is fixed (10)
%   correction_for_time_since_placement - Correction for time to fix due to the
%                         time since the bug was placed ('x')
%   complexity_function - The reduction in effective time due to complexity,
%                         in the format 'fun(x)' where x is a time value ('x')
%   bugs_per_line       - The number of bugs per line of code (0.01)
%   bugs                - The bug prototype that will be used
%
% Output:
%   time_to_execute     - The time taken to fix the bugs
%   program_structure   - A modified program structure
%
% Change the defaults using the pair: 'param', param value

time_to_fix         = 2;
lines_to_fix        = 10;
correction_for_time_since_placement = '(x)/2000';
complexity_function = '(x)';
bugs                = bug;
bugs_per_line       = 0.01;

possible_params         = {'bugs_per_line', 'bugs', 'time_to_fix', 'lines_to_fix', 'correction_for_time_since_placement', 'complexity_function'};

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

time_to_execute     = 0;
new_time_to_fix     = eval(strrep(complexity_function, 'x', 'time_to_fix'));
if (new_time_to_fix < time_to_fix)
    warning('Complexity function decreased the effective time!');
end
time_to_fix         = new_time_to_fix;

correction_for_time_since_placement = strrep(correction_for_time_since_placement, 'x', 'dt');
new_bugs            = round(lines_to_fix * bugs_per_line);

for i = 1:length(bugs_found)
    dt              = program_structure.lines - program_structure.bugs(bugs_found(i)).line + 1;
    time_to_execute = time_to_execute + time_to_fix * (1 + eval(correction_for_time_since_placement));
    
    %Fix the bug
    program_structure.bugs(bugs_found(i)).fixed = 1;
    
    %Add new bugs
    bug_fields  = fields(bugs);
    for k = 1:new_bugs
        %Where?
        cur_line    = max(1, min(program_structure.lines, round(rand(1)*program_structure.lines)));

        %Insert
        Nbug        = length(program_structure.bugs) + 1;
        program_structure.bugs(Nbug).line           = cur_line;
        program_structure.bugs(Nbug).insert_time    = program_structure.time;
        program_structure.bugs(Nbug).fixed          = 0;
        program_structure.bugs(Nbug).tests_run      = [];
        for j = 1:length(bug_fields)
            eval(['program_structure.bugs(Nbug).' bug_fields{j} '=bugs.' bug_fields{j} ';']);
        end
    end
end
