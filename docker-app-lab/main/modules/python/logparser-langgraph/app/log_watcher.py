import os
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

class LogHandler(FileSystemEventHandler):
    def __init__(self, on_new_log_callback):
        self.on_new_log_callback = on_new_log_callback

    def on_modified(self, event):
        if event.src_path.endswith(".log"):
            with open(event.src_path, "r") as f:
                last_line = f.readlines()[-1]
                state_update = {"latest_log": last_line}
                self.on_new_log_callback(state_update)  # 👈 emit plain dict

def start_tailing(log_dir, callback):
    observer = Observer()
    observer.schedule(LogHandler(callback), path=log_dir, recursive=False)
    observer.start()