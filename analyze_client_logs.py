import numpy as np
import os
import sys
from log_analysis.experiment import Experiment
from log_analysis.multiexperiment import MultiExperiment

#def fetch_times(url, start_time):
#    """Fetches the operation timings from the puppet master given its address.
#
#    Keyword arguments:
#    url -- the url of the puppet master
#
#    Returns the global times matrix
#    """
#    req = requests.get(url + '/client')
#    timings = req.json()['timings']
#    id_to_times = np.array(list(map(lambda j: [j['id'], j['start'], j['end']], timings)))
#    end_time = np.max(id_to_times[:, 1:]) - start_time
#    hist = None
#    for client in np.unique(id_to_times[:, 0]):
#        client_times = id_to_times[id_to_times[:, 0] == client][:, 1:] - start_time
#        client_hist = compute_buckets(client_times, 1000, end_time)
#        if hist is None:
#            hist = client_hist
#        else:
#            hist += client_hist
#    return hist


if __name__ == "__main__":
    if len(sys.argv) > 2:
        R = int(sys.argv[1])
        N = int(sys.argv[2])
        events = [np.int(time) for time in sys.argv[3:-1]]
        start_time = events[0]
        events = [time - start_time for time in events]

    #buckets = fetch_times("http://localhost:9090", start_time)
    #buckets = compute_buckets(matrix, 1000, events[-1])
    #exp = Experiment('BestLeader', R, N, events[1:-1], buckets, start_time)
    #exp.plot()
        filename = f'{sys.argv[-1]}.pkl'
        exp = Experiment.from_logs('logs', events, start_time)
        exp.save(filename)
        #exp.plot()
    else:
        exps = []
        for f in filter(lambda n: 'pkl' in n, os.listdir()):
            exps.append(Experiment.from_pickle(f))
        me = MultiExperiment(exps)
        me.plot()

