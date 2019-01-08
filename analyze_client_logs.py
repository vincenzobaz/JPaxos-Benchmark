import numpy as np
import os
import sys
from log_analysis.experiment import Experiment
from log_analysis.multiexperiment import MultiExperiment

if __name__ == "__main__":
    print(sys.argv)
    if len(sys.argv) > 1 and len(sys.argv) < 3:
        exp = Experiment.from_puppetmaster("provaPM", 'http://localhost:9090')
        exp.save(sys.argv[1])
        #exp.plot()
    elif len(sys.argv) > 2:
        R = int(sys.argv[1])
        N = int(sys.argv[2])
        events = [np.int(time) for time in sys.argv[3:-1]]
        start_time = events[0]
        events = [time - start_time for time in events]
        filename = f'{sys.argv[-1]}.pkl'
        exp = Experiment.from_logs('logs', events, start_time)
        exp.save(filename)
        exp.plot()
    else:
        exps = []
        for f in filter(lambda n: 'pkl' in n, os.listdir()):
            exps.append(Experiment.from_pickle(f))
        me = MultiExperiment(exps)
        me.plot()

