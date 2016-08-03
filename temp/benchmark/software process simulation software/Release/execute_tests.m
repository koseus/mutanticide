function [bug_numbers, program_structure, tests, time_so_far] = execute_tests(program_structure, tests, execution_time, varargin)

% Execute tests
%
% Required inputs:
%   program_structure   - A programming structure of the programming so far
%   tests               - The tests to execute
%   execution_time      - The time that the step will be carried out
%
% Optional inputs (defaults):
%   only_new_tests      - Execute only new tests (0)
%   test_numbers        - A vector with the test numbers to execute (all)
%   time_so_far         - Actual execution time
%
% Output:
%   bug_numbers         - The numbers of the detected bugs
%   program_structure   - The modified program structure
%   tests               - The modified tests structure
%
% Change the defaults using the pair: 'param', param value

possible_params         = {'only_new_tests', 'test_numbers'};

only_new_tests          = 0;
test_numbers            = 1:length(tests);

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

time_so_far = 0;
test_number = length(tests)+1;
bug_numbers = [];

while (test_number > 1) 
    test_number = test_number - 1;
    if ~only_new_tests | (tests(test_number).new == 1)
        if (time_so_far + tests(test_number).time_to_execute <= execution_time)
            % Can execute
            tests(test_number).new  = 0;
            time_so_far             = time_so_far + tests(test_number).time_to_execute;

            % Which are the relevant bugs?
            relevant_bugs           = find(ismember([program_structure.bugs(:).line], tests(test_number).covered_lines) & ~[program_structure.bugs(:).fixed]);
            for i = 1:length(relevant_bugs)
                %Was this test run on this bug before?
                if any(program_structure.bugs(relevant_bugs(i)).tests_run == test_number)
                    %Yes
                    Pdetect = program_structure.bugs(relevant_bugs(i)).Pdeter;
                else
                    %No
                    Pdetect = program_structure.bugs(relevant_bugs(i)).Ptest;
                end
                
                %Is this bug found during this test?
                if (rand(1) < Pdetect)
                    %Found
                    bug_numbers(end+1)  = relevant_bugs(i);                    
                end
                
                program_structure.bugs(relevant_bugs(i)).tests_run(end+1)    = test_number;                
            end
        else
            break
        end
    end
end
    
bug_numbers	= unique(bug_numbers);    
    
    