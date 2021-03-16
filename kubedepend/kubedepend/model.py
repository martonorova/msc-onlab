import csv
import time
import subprocess

class BackendDependabilityMetrics:
    def __init__(self):
        self.availability = None
        # Mean Up Time
        self.mut = None
        # Mean Down Time
        self.mdt = None
        # Mean Time Between Failures
        self.mtbf = None

    def asdict(self):
        return {
            'availability': self.availability,
            'mut': self.mut,
            'mdt': self.mdt,
            'mtbf': self.mtbf
        }
    
    def __str__(self):
        return (
            f'BackendDependabilityMetrics( Availability={self.availability}, '
            f'Mean Up Time={self.mut}, '
            f'Mean Down Time={self.mdt}, '
            f'Mean Time Between Failures={self.mtbf} )'
        )
        
    
class SystemState:
    def __init__(self):
        self.needed_worker_ratio = None
        self.queue_size = None
        self.worker_busy_threads_count = None
        self.worker_pods_count = None

class TestResult:
    def __init__(self, metrics=[]):
        self.metrics = []

        if not isinstance(metrics, list):
            raise ValueError('metrics must be an array')
        self.metrics = metrics

    def add_metric(self, metric):
        if not isinstance(metric, BackendDependabilityMetrics):
            raise ValueError('metric must be BackendDependabilityMetrics')
        self.metrics.append(metric)
    
    def save_results(self, filename):

        now = int(time.time())
        git_commit_short = subprocess.check_output(['git', 'rev-parse', '--short', 'HEAD']).decode('ascii').strip()

        # open(filename, mode='x').close()

        with open(filename, mode='w') as csv_file:
            fieldnames = ['timestamp'] + list(self.metrics[0].asdict().keys()) + ['prev_git_commit_short']
            print(fieldnames)
            writer = csv.DictWriter(csv_file, fieldnames=fieldnames)

            writer.writeheader()
            for metric in self.metrics:
                row = metric.asdict()
                row['timestamp'] = now
                row['prev_git_commit_short'] = git_commit_short
                writer.writerow(row)

