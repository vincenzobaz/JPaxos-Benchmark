from log_analysis.experiment import Experiment
import matplotlib.pyplot as plt

class MultiExperiment:
    def __init__(self, experiments, title=''):
        self.experiments = experiments
        self.title = title

    def plot(self, title):
        fig, (ax, axz) = plt.subplots(nrows=1, ncols=2, figsize=(15, 4))
        #colors = ['orange', 'g', 'b']
        for color, exp in enumerate(self.experiments):
            ax.plot(exp.throughput, label=exp.name.split('/')[-1])#, c=colors[color])
            axz.plot(exp.throughput, label=exp.name.split('/')[-1])#, c=colors[color])
            for ev in exp.events:
                ax.axvline(x=(ev / 1000))#, c=colors[color])
        axz.set_xlim(left=100, right=120)

        ax.set_xlabel('Time (s)')
        ax.set_ylabel('Throughput (#Ops / s)')
        ax.set_title(title)
        ax.legend()
        
        axz.set_xlabel('Time (s)')
        axz.set_ylabel('Throughput (#Ops / s)')
        axz.set_title(f'{title} (zoom)')
        axz.legend()
        plt.show()
        return fig
