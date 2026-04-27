from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

class LogHandler(FileSystemEventHandler):
    def on_modified(self, event):
        if event.src_path.endswith(".log"):
            with open(event.src_path, "r") as f:
                last_line = f.readlines()[-1]
                # Trigger LangGraph node here or append to state
                print("New log line:", last_line)

def start_tailing(path="/app/logs"):
    observer = Observer()
    observer.schedule(LogHandler(), path=path, recursive=False)
    observer.start()