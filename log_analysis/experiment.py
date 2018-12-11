import matplotlib.pyplot as plt
from log_analysis.data_processing import *
import requests
import os
import pickle


class Experiment:
    def __init__(self, name, replicas, clients, events, throughput, start_date):
        self.name = name
        self.events = events
        self.throughput = throughput
        self.start_date = start_date
        self.replicas = replicas
        self.clients = clients

    def plot(self):
        plt.plot(self.throughput)
        for ev in self.events:
            plt.axvline(x=(ev / 1000), c='r')

        plt.title(f'Throughput with {self.replicas} replicas, {self.clients} clients')
        plt.xlabel('Time (s)')
        plt.ylabel('Throughput (#Ops / s)')
        plt.show()

    def save(self, filename):
        with open(filename, 'wb') as pkl:
            pickle.dump(self.to_dict(), pkl)

    @classmethod
    def from_logs(cls, logfolder, events, start_time, name=''):
        R = 0
        matrices = []
        for filename in os.listdir('logs'):
            if 'replica' in filename:
                R += 1
            else:
                with open(f'{logfolder}/{filename}') as f:
                    matrices.append(create_time_matrix_client(list(f)))

        N = len(matrices)
        matrix = create_global_matrix(matrices, start_time)
        thr = compute_buckets(matrix, 1000, events[-1])
        return cls(name, R, N, events[1:-1], thr, start_time)

    @classmethod
    def from_pickle(cls, filename):
        pkl = open(filename, 'rb')
        data = pickle.load(pkl)
        data['name'] = filename.split('.')[0]
        pkl.close()
        return cls(**data)

    def to_dict(self):
        return {
            'name': self.name,
            'replicas': self.replicas,
            'clients': self.clients,
            'events': self.events,
            'throughput': self.throughput,
            'start_date': self.start_date
        }


    @classmethod
    def from_puppetmaster(cls, name, url):
        events = requests.get(url + '/control-events').json() #Array[{time, label}]
        events = np.array(list(map(lambda ev: ev['time'], events)))
        views = requests.get(url + '/leaders').json() #Array[{time, localId, newView, newLeader}]
        timings = requests.get(url + '/client').json() #Array[{id, start, end}]
        id_to_times = np.array(list(map(lambda j: [j['id'], j['start'], j['end']], timings)))

        R = len(set((ob['localId'] for ob in views)))
        C = len(np.unique(id_to_times[:, 0]))

        start_time = min(np.min(events), np.min(id_to_times[:, 1:]))
        end_time = max(np.max(id_to_times[:, 1:]), np.max(events)) - start_time
        hist = None
        for client in np.unique(id_to_times[:, 0]):
            this_client = id_to_times[id_to_times[:, 0] == client]
            this_client = this_client[:, 1:] - start_time
            client_hist = compute_buckets(this_client, 1000, end_time)
            if hist is None:
                hist = client_hist
            else:
                hist += client_hist
        return cls(name, R, C, (events[1:-1] - start_time), hist, start_time)

