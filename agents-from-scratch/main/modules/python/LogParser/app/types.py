from typing import TypedDict, Annotated
import operator

class LogState(TypedDict):
    log_lines: list[str]
    errors: Annotated[list[str], operator.add]
    level_counts: Annotated[dict[str, int], operator.or_]
    anomalies: Annotated[list[str], operator.add]
    log_path: str