import numpy as np
import math


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


def compute_buckets(times_matrix, bucket_size, end_date):
    """Computes the throughput.

    Keyword arguments:
    times_matrix -- the matrix of milliseconds times |start|end|
    bucket_size -- the width of the bucket in milliseconds

    Returns a numpy array containing the fraction of operations in each bucket
    """
    bucket_count = math.ceil(end_date / bucket_size) + 1
    buckets = np.zeros((bucket_count + 2,))

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

