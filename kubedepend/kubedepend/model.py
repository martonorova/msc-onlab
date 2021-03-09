class BackendDependabilityMetrics:
    def __init__(self):
        self.availability = None
        # Mean Up Time
        self.mut = None
        # Mean Down Time
        self.mdt = None
        # Mean Time Between Failures
        self.mtbf = None
    
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
    