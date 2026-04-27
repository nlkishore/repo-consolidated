import os
import argparse
import subprocess
from app.config_loader import load_config
from app.logger import get_logger
from app.graph import start_graph_runtime

logger = get_logger()

def run_cli(config):
    logger.info("Launching CLI mode")
    start_graph_runtime(mode="cli", config=config)

def run_dashboard(config):
    logger.info("Launching Streamlit dashboard")
    subprocess.run(["streamlit", "run", "app/dashboard.py"])

def main():
    parser = argparse.ArgumentParser(description="LangGraph Log Analyzer")
    parser.add_argument("--cli", action="store_true", help="Force CLI mode")
    parser.add_argument("--config", type=str, default="config/settings.yaml", help="Path to config file")
    args = parser.parse_args()

    # Load YAML config
    config = load_config(args.config)

    # Determine mode
    env_mode = os.getenv("MODE", None)
    config_mode = config.get("mode", "gui").lower()
    final_mode = "cli" if args.cli or env_mode == "cli" or config_mode == "cli" else "gui"

    logger.info(f"Final mode selected: {final_mode.upper()}")

    if final_mode == "cli":
        run_cli(config)
    else:
        run_dashboard(config)

if __name__ == "__main__":
    main()