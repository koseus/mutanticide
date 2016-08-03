function tests  = write_tests(program_structure, writing_time, varargin)

% Write new tests
%
% Required inputs:
%   program_structure   - A programming structure of the programming so far
%   writing_time        - The time that the step will be carried out
%
% Optional inputs (defaults):
%   max_line            - Maximum line number for the test (Current program length)
%   min_coverage_line   - The first line of the program covered by the test
%   max_coverage_line   - The last line of the program covered by the test
%   percent_coverage_old- Percent of the old code covered by the test (0.1)
%   percent_coverage_new- Percent of the new code covered by the test (0.1)
%   time_to_write       - Time to write a single test (3)
%   time_to_execute     - Time to execute a single test (1)
%   complexity_function - The reduction in effective time due to complexity,
%                         in the format 'fun(x)' where x is a time value ('x')
%
% If max_line is greater than current program length, the defaults are:
%       min_coverage_line: Current program length
%       max_coverage_line: max_line
% Otherwise:
%       min_coverage_line: 1
%       max_coverage_line: Current program length
%
% Output:
%       tests           - The new tests
%
% Change the defaults using the pair: 'param', param value

possible_params         = {'max_line', 'min_coverage_line', 'max_coverage_line', 'time_to_execute', 'time_to_write', ...
                           'percent_coverage_old', 'percent_coverage_new', 'time_per_test', 'complexity_function'};

percent_coverage_old    = 0.1;
percent_coverage_new    = 0.1;
time_to_write           = 3;
time_to_execute         = 1;
max_line                = program_structure.lines;
complexity_function     = '(x)';

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

if ~exist('min_coverage_line')
    if (max_line > program_structure.lines)
        min_coverage_line = program_structure.lines;
    else
        min_coverage_line = 1;
    end
end
if ~exist('max_coverage_line')
    if (max_line > program_structure.lines)
        max_coverage_line = max_line;
    else
        max_coverage_line = program_structure.lines;
    end
end

%Re-estimate time
new_writing_time    = eval(strrep(complexity_function, 'x', 'writing_time'));
if (new_writing_time > writing_time)
    warning('Complexity function increased the effective time!');
end
writing_time    = new_writing_time;

Ntests  = floor(writing_time / time_to_write);

for i = 1:Ntests,
    old_code_lines  = [min_coverage_line:program_structure.lines];
    new_code_lines  = [program_structure.lines+1:max_coverage_line];
    old_code_lines  = old_code_lines(randperm(length(old_code_lines)));
    new_code_lines  = new_code_lines(randperm(length(new_code_lines)));
    
    tests(i).covered_lines  = sort([old_code_lines(1:floor(percent_coverage_old*length(old_code_lines))), new_code_lines(1:floor(percent_coverage_new*length(new_code_lines)))]);
    tests(i).new            = 1;
    tests(i).time_to_execute= time_to_execute;
end
