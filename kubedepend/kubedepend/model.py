import csv
import time
import subprocess
import uuid
import os.path
from datetime import datetime

import constants as c


class BackendMetrics:
    def __init__(self, availability=None, mut=None, mdt=None, mtbf=None):
        self.availability = availability
        # Mean Up Time
        self.mut = mut
        # Mean Down Time
        self.mdt = mdt
        # Mean Time Between Failures
        self.mtbf = mtbf


class MeasurementResult:
    def __init__(self):
        # backend metrics
        self.backend_metrics = None
        # Start time of this single measurement
        # self.start_time = time.time()
        self.start_time = datetime.now().strftime("%m-%d-%Y_%H-%M-%S.%f")
        # End time of this single measurement
        self.end_time = None

    def asdict(self):
        return {
            'availability': self.backend_metrics.availability,
            'mut': self.backend_metrics.mut,
            'mdt': self.backend_metrics.mdt,
            'mtbf': self.backend_metrics.mtbf,
            'measurement_start_time': self.start_time,
            'measurement_end_time': self.end_time
        }

    def end(self):
        self.end_time = datetime.now().strftime("%m-%d-%Y_%H-%M-%S.%f")

    def __str__(self):
        return (
            f'MeasurementResult( Availability={self.backend_metrics.availability}, '
            f'Mean Up Time={self.backend_metrics.mut}, '
            f'Mean Down Time={self.backend_metrics.mdt}, '
            f'Mean Time Between Failures={self.backend_metrics.mtbf} '
            f'Start Time of Measurement={self.start_time} '
            f'End Time of Measurement={self.end_time} )'
        )


class SystemState:
    def __init__(self):
        self.needed_worker_ratio = None
        self.queue_size = None
        self.worker_busy_threads_count = None
        self.worker_pods_count = None


class MeasurementSequenceResult:
    def __init__(self,
                 start_time,
                 fault_profile,
                 cluster_type,
                 load_duration,
                 locust_user_count,
                 locust_spawn_rate,
                 comment,
                 measurements=[]):

        self.id = uuid.uuid4()
        self.start_time = start_time
        self.fault_profile = fault_profile
        self.cluster_type = cluster_type
        self.load_duration = load_duration
        self.locust_user_count = locust_user_count
        self.locust_spawn_rate = locust_spawn_rate
        self.comment = comment
        self.measurements = None

        if not isinstance(measurements, list):
            raise ValueError('metrics must be an array')
        self.measurements = measurements

    def add_measurement_result(self, metric):
        if not isinstance(metric, MeasurementResult):
            raise ValueError('metric must be MeasurementResult')
        self.measurements.append(metric)

    def save_results(self, filename):
        # if there are result values
        if self.measurements:
            git_commit_short = subprocess.check_output(
                ['git', 'rev-parse', '--short', 'HEAD'], cwd=c.PATH_TO_STACK_REPO).decode('ascii').strip()

            with open(filename, mode='a+') as csv_file:
                fieldnames = ['id', 'measurement_seq_start_time'] \
                    + list(self.measurements[0].asdict().keys()) \
                    + [
                        'fault_profile',
                        'cluster_type',
                        'measurement_count',
                        'load_duration',
                        'locust_user_count',
                        'locust_spawn_rate',
                        'prev_stack_git_commit_short',
                        'comment']

                #print(fieldnames)
                writer = csv.DictWriter(csv_file, fieldnames=fieldnames)

                if os.path.getsize(filename) == 0:
                    writer.writeheader()

                for measurement in self.measurements:
                    row = measurement.asdict()
                    row['id'] = self.id
                    row['measurement_seq_start_time'] = self.start_time
                    row['fault_profile'] = self.fault_profile
                    row['cluster_type'] = self.cluster_type
                    row['measurement_count'] = len(self.measurements)
                    row['load_duration'] = self.load_duration
                    row['locust_user_count'] = self.locust_user_count
                    row['locust_spawn_rate'] = self.locust_spawn_rate
                    row['prev_stack_git_commit_short'] = git_commit_short
                    row['comment'] = self.comment

                    writer.writerow(row)
