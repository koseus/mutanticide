function bug_structure  = bug (varargin)

% Define the defaults for a bug.
%
% Possible parameters (defaults):
%   Ptest   - Probability for discovering the bug by a test (0.5)                      
%   Pdeter  - Probability factor for discovering the bug by a rerun of the
%             same test (0.0)
%   Preview - Probability for discovering the bug by a review (0.5)
%
% Change the defaults using the pair: 'param', param value

bug_structure.Ptest     = 0.5;
bug_structure.Pdeter    = 0.0;
bug_structure.Preview   = 0.5;

possible_params         = {'Ptest', 'Pdeter', 'Preview'};

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
