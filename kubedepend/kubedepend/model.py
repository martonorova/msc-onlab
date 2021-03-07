class BackendDependabilityAttributes:
    def __init__(self):
        self.availability = None
        # Mean Up Time
        self.mut = None
        # Mean Down Time
        self.mdt = None
        # Mean Time Between Failures
        self.mtbf = None
    
class SystemState:
    def __init__(self):
        self.needed_worker_ratio = None
        self.queue_size = None
        self.worker_busy_threads_count = None
        self.worker_pods_count = None
    