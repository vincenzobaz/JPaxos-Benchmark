import numpy as np
import math
import matplotlib.pyplot as plt
import sys


def create_time_matrix_client(data):
    """Creates the matrix of times given the rows of the log of ONE CLIENT.
    The resulting matrix has two columns: |start|end|
    Time durations are expressed in milliseconds.

    Keyword arguments:
    data -- the rows of the log file, a list of strings
    """
    starts = filter(lambda r: 'Requested operation' in r, data)
    ends = filter(lambda r: 'Completed operation' in r, data)

    extract_time = lambda ev: np.int(ev.split('\t')[0])

    starts = np.array(list(map(extract_time, starts)))
    ends = np.array(list(map(extract_time, ends)))

    # Make sure both have same length
    events_count = np.min([len(starts), len(ends)])
    starts = starts[:events_count]
    ends = ends[:events_count]

    times = np.stack([starts, ends]).T
    return times


def create_global_matrix(matrices, start_time):
    """Assembles the time matrices of each client into a global one.

    Keyword arguments:
    matrices -- a list of matrix, one per client
    """
    global_mat = np.vstack(matrices)
    global_mat -= start_time
    return global_mat


def read_logs(count):
    matrices = []
    for i in range(count):
        f = open(f'logs/client{i}.out', 'r')
        matrices.append(create_time_matrix_client(list(f)))
        f.close()
    return matrices


def compute_buckets(times_matrix, bucket_size, end_date):
    """Computes the throughput.

    Keyword arguments:
    times_matrix -- the mmatrix of milliseconds times |start|end|duration
    bucket_size -- the width of the bucket in milliseconds

    Returns a numpy array containing the fraction of operations in each bucket
    """
    bucket_count = math.ceil(end_date / bucket_size) + 1
    buckets = np.zeros((bucket_count,))

    for start, end in times_matrix:
        duration = end - start
        start_index = math.floor(start / bucket_size)
        end_index = math.floor(end / bucket_size)
        if start_index == end_index:
            buckets[start_index] += 1
        else:
            buckets[start_index] += (((start_index + 1)*bucket_size - start) / duration)
            buckets[np.arange(start_index + 1, end_index)] += (bucket_size / duration)
            buckets[end_index] += ((end - end_index * bucket_size) / duration)

    return buckets


if __name__ == "__main__":
    R = int(sys.argv[1])
    N = int(sys.argv[2])
    events = [np.int(time) for time in sys.argv[3:]]
    start_time = events[0]
    events = [time - start_time for time in events]
    matrix = create_global_matrix(read_logs(N), start_time)

    buckets = compute_buckets(matrix, 1000, events[-1])
    plt.plot(buckets)

    for ev in events[1:-1]:
        plt.axvline(x=(ev / 1000), c='r')

    plt.title(f'Throughput with {R} replicas, {N} clients')
    plt.xlabel('Time (s)')
    plt.ylabel('Throughput (#Ops / s)')
    plt.show()

