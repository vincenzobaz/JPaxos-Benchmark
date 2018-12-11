from log_analysis.experiment import Experiment
import matplotlib.pyplot as plt

class MultiExperiment:
    def __init__(self, experiments, title=''):
        self.experiments = experiments
        self.title = title

    def plot(self):
        fig = plt.figure()
        ax = plt.subplot(111)
        colors = ['orange', 'g', 'b']
        for color, exp in enumerate(self.experiments):
            ax.plot(exp.throughput, label=exp.name)#, c=colors[color])
            for ev in exp.events:
                ax.axvline(x=(ev / 1000))#, c=colors[color])

        plt.xlabel('Time (s)')
        plt.ylabel('Throughput (#Ops / s)')
        ax.legend()
        plt.show()

